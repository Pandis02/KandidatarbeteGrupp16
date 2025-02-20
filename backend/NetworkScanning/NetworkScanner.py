import nmap
import requests
import logging
from datetime import datetime

API_URL = "http://localhost:8080/api/scans/add"

# Configure logger
logging.basicConfig(level=logging.INFO)  
logger = logging.getLogger(__name__)

class NetworkScanner:
    """Scans the network with Nmap and sends results to a REST API."""

    def __init__(self, network_range="192.168.86.0/24"):
        self.network_range = network_range
        self.scanner = nmap.PortScanner()

    def scan_network(self):
        """Perform a network scan and send results to the API."""
        logger.info(f"🔍 Scanning the network {self.network_range} with Nmap...")

        try:
            self.scanner.scan(hosts=self.network_range, arguments="-sn -R")
        except nmap.PortScannerError as e:
            logger.error(f"🔴 Nmap scan failed: {e}")
            return {}

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

            logger.info(f"Preparing to send data: {device_data}")  # Debugging output

            # API Request with error handling
            try:
                response = requests.post(API_URL, json=device_data, timeout=5)  # Set timeout to prevent hanging
                response.raise_for_status()  # Raises an exception for HTTP error codes (4xx, 5xx)

                logger.info(f"🟢  Sent {ip} to API successfully!")
            except requests.exceptions.ConnectionError:
                logger.error(f"🔴 Failed to connect to API. Is the backend running?")
            except requests.exceptions.Timeout:
                logger.error(f"🔴 Timeout error: The API took too long to respond.")
            except requests.exceptions.HTTPError as e:
                logger.error(f"🔴 API returned an HTTP error: {e.response.status_code} - {e.response.text}")
            except requests.exceptions.RequestException as e:
                logger.error(f"🔴 An unexpected error occurred: {e}")

if __name__ == "__main__":
    network_range = input("Enter the network range (e.g., 192.168.1.0/24): ")
    scanner = NetworkScanner(network_range)
    scanner.scan_network()
