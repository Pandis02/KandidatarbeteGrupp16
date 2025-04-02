package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"strings"
	"time"
)

const server = "http://localhost:8080/checkin"

var interval = 15

type RequestBody struct {
	MacAddress string `json:"macAddress"`
	Hostname   string `json:"hostname"`
}

func main() {
	hostname, err := os.Hostname()
	if err != nil {
		fmt.Println("Error finding hostname:", err)
		return
	}

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
	fmt.Printf("Started checkin routine from %s(%s) to: %s\n", hostname, mac, server)
	for {
		newInterval, err := checkin(mac, hostname)
		if err != nil {
			fmt.Println("Error checking in:", err)
			time.Sleep(time.Second * 1)
			continue
		} else {
			fmt.Println("Checked in!")
			// check if we got a new interval
			if interval != newInterval {
				interval = newInterval
				fmt.Printf("New interval %v set! \n", interval)
			}
		}

		time.Sleep(time.Second * time.Duration(interval))
	}
}

func checkin(mac string, hostname string) (interval int, err error) {
	// Prepare body
	jsonData, err := json.Marshal(RequestBody{MacAddress: mac, Hostname: hostname})
	if err != nil {
		return 0, err
	}

	// Create a new request
	req, err := http.NewRequest("POST", server, bytes.NewBuffer(jsonData))
	if err != nil {
		return 0, err
	}

	// Set headers
	req.Header.Set("Content-Type", "application/json")

	// Send the request
	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		if strings.HasSuffix(err.Error(), "because the target machine actively refused it.") {
			return 0, errors.New("server is offline!")
		}
		return 0, err
	}
	defer resp.Body.Close()

	// Read the response body
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return 0, err
	}

	response := map[string]any{}
	err = json.Unmarshal(body, &response)
	if err != nil {
		return 0, err
	}

	if response["success"] == true {
		return int(response["interval"].(float64)), nil
	} else if response["success"] == false {
		return 0, errors.New(response["message"].(string))
	}

	return 0, errors.New("unexpected response -> " + err.Error())
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
