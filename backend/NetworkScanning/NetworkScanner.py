import nmap

class NmapScanner:
    """Scanns the network with Nmap and returns a dictionary {IP: (Status, Hostname, MAC, Last seen)}."""

    def __init__(self, network_range="192.168.86.0/24"):
        self.network_range = network_range
        self.scanner = nmap.PortScanner()

    def scan_network(self):
        """Perform a network scan and return a dictionary with IP, status and hostname"""
        print(f"🔍 Scanns the network {self.network_range} with Nmap...\n")
        self.scanner.scan(hosts=self.network_range, arguments="-sn -R")  # -R för Reverse DNS lookup

        devices = {} # Dictionary 
        for host in self.scanner.all_hosts():
            status = self.scanner[host].state() == "up"  # True/False
            mac_address = self.scanner[host]['addresses'].get('mac', 'Unknown MAC')
            hostname = self.scanner[host].hostname() or "Unknown unit" # Fetch hostname, or empty string if missing 
            last_seen = self.scanner.scanstats().get('timestr', 'Unknown Time') 
            devices[host] = (status, hostname, mac_address, last_seen)  

        return devices  # {IP: (True/False, "Hostname")}

if __name__ == "__main__":
    ip_address = input("Enter your IP address: ")
    scanner = NmapScanner(ip_address + '/24')# Change to your network
    results = scanner.scan_network()

    print("\n📋 Explore units")
    for ip, (status, hostname, mac_address, last_seen) in results.items():
        status_text = "🟢 Online" if status else "🔴 Offline"
        print(f"IP: {ip}  |  Status: {status_text}  |  Name: {hostname} | MAC: {mac_address} | Last seen:  {last_seen}")




