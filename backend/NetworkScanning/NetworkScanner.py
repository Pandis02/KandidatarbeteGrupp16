import nmap
import requests
from datetime import datetime

API_URL = "http://localhost:8080/api/scans/add"  

class NetworkScanner:
    """Scans the network with Nmap and sends results to a REST API."""

    def __init__(self, network_range="192.168.86.0/24"):
        self.network_range = network_range
        self.scanner = nmap.PortScanner()

    def scan_network(self):
        """Perform a network scan and send results to the API."""
        print(f"🔍 Scanning the network {self.network_range} with Nmap...\n")

        try:
            self.scanner.scan(hosts=self.network_range, arguments="-sn -R")
        except nmap.PortScannerError as e:
            print(f"🔴 Nmap scan failed: {e}")
            return

        for ip in self.scanner.all_hosts():
            status = 1 if self.scanner[ip].state() == "up" else 0
            mac_address = self.scanner[ip].get('addresses', {}).get('mac', 'Unknown MAC')
            hostname = self.scanner[ip].hostname() or "Unknown"
            last_seen = datetime.now().isoformat() 

            device_data = {
                "ipAddress": ip,  
                "hostname": hostname,
                "macAddress": mac_address,
                "status": status,
                "lastSeen": last_seen  
            }

            print(f"Sending data: {device_data}")  # Debugging output

            response = requests.post(API_URL, json=device_data)
            if response.status_code == 200:
                print(f"🟢  Sent {ip} to API successfully!")
            else:
                print(f"🔴  Failed to send {ip}: {response.text}")

if __name__ == "__main__":
    network_range = input("Enter the network range (e.g., 192.168.1.0/24): ")
    scanner = NetworkScanner(network_range)
    scanner.scan_network()
