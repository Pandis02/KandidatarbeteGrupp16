import nmap
from datetime import datetime

class NmapScanner:
    """Scanns the network with Nmap and returns a dictionary {IP: (Status, Hostname, MAC, Last seen)}."""

    def __init__(self, network_range="192.168.86.0/24"):
        self.network_range = network_range
        self.scanner = nmap.PortScanner()

    def scan_network(self):
        """Perform a network scan and return a dictionary with IP, status and hostname"""
        print(f"🔍 Scanning the network {self.network_range} with Nmap...\n")

        try: # Adding error handling
            self.scanner.scan(hosts=self.network_range, arguments="-sn -R")
        except nmap.PortScannerError as e:
            print(f"Nmap scan failed: {e}")
            return {}

        devices = {} # Dictionary 
        for host in self.scanner.all_hosts():
            status = self.scanner[host].state() == "up"  # True/False
            mac_address = self.scanner[host].get('addresses', {}).get('mac', 'Unknown MAC') # If MAC is missing, return "Unknown MAC"
            hostname = self.scanner[host].hostname() or "Unknown unit" # Fetch hostname, or empty string if missing 
            last_seen = datetime.now().strftime("%Y-%m-%d %H:%M:%S") # Changed from Nmap time to local time to avoid "unknown time" or time when scan started if scan takes a long time
            devices[host] = (status, hostname, mac_address, last_seen)  

        return devices  # {IP: (True/False, "Hostname")}

if __name__ == "__main__":
    network_range = input("Enter the network range (e.g., 192.168.1.0/24): ") # Let the user decide the subnet range
    scanner = NmapScanner(network_range)

    results = scanner.scan_network()

    print("\n📋 Explore units")
    for ip, (status, hostname, mac_address, last_seen) in results.items():
        status_text = "🟢 Online" if status else "🔴 Offline"
        print(f"IP: {ip}  |  Status: {status_text}  |  Name: {hostname} | MAC: {mac_address} | Last seen:  {last_seen}")




