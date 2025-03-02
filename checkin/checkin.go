package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net"
	"net/http"
	"strings"
	"time"
)

const reciever = "http://localhost:8080/checkin"
const waitTime = time.Second * 15

type RequestBody struct {
	MacAddress string `json:"macAddress"`
}

func main() {
	// Get the interface that's most likely to be used for internet connectivity
	primaryInterface, err := getPrimaryInterface()
	if err != nil {
		fmt.Println("Error finding primary interface:", err)
		return
	}

	// Currently looks like 04:7c:16:de:4e:08
	mac := strings.ToUpper(primaryInterface.HardwareAddr.String())
	mac = strings.ReplaceAll(mac, ":", "-")
	// Now looks like 04-7C-16-DE-4E-08

	// Forever while loop
	fmt.Printf("Started checkin routine with MAC: %s to HOST: %s\n", mac, reciever)
	for {
		err := checkin(mac)
		if err != nil {
			if strings.HasSuffix(err.Error(), "because the target machine actively refused it.") {
				fmt.Println("Error: reciever is offline!")
			} else {
				fmt.Printf("Error: %v\n", err)
			}
		}

		fmt.Println("Checked in!")
		time.Sleep(waitTime)
	}
}

func getPrimaryInterface() (net.Interface, error) {
	// This is a common approach to find the primary interface since there will be many:
	// Create a UDP connection to a public IP (doesn't actually connect)
	// Then check which local interface would be used for this connection
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err != nil {
		return net.Interface{}, err
	}
	defer conn.Close()

	localAddr := conn.LocalAddr().(*net.UDPAddr)

	// Get all interfaces
	interfaces, err := net.Interfaces()
	if err != nil {
		return net.Interface{}, err
	}

	// Find the interface that has this IP address
	for _, iface := range interfaces {
		addrs, err := iface.Addrs()
		if err != nil {
			continue
		}

		for _, addr := range addrs {
			ipNet, ok := addr.(*net.IPNet)
			if ok && !ipNet.IP.IsLoopback() {
				if ipNet.IP.Equal(localAddr.IP) {
					return iface, nil
				}
			}
		}
	}

	return net.Interface{}, fmt.Errorf("no matching interface found")
}

func checkin(mac string) error {
	// Prepare body
	jsonData, err := json.Marshal(RequestBody{MacAddress: mac})
	if err != nil {
		return err
	}

	// Create a new request
	req, err := http.NewRequest("POST", reciever, bytes.NewBuffer(jsonData))
	if err != nil {
		return err
	}

	// Set headers
	req.Header.Set("Content-Type", "application/json")

	// Send the request
	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	return nil
}
