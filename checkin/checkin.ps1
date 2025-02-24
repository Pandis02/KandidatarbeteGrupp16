# Get our MAC address
$macAddress = (Get-NetAdapter | Where-Object { $_.Status -match "Up" } | Select-Object -ExpandProperty MacAddress)

#Write-Output $macAddress

# Define the URL
$url = "http://localhost:8080/checkin"

# Prepare the JSON body with correct field name
$body = @{ macAddress = $macAddress } | ConvertTo-Json -Depth 10

# Send the POST request
Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json"
