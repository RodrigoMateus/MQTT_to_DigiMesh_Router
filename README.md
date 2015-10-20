# MQTT_to_DigiMesh_Router
Projeto TCC - roteador de mensagem entre MQTT e rede DigiMesh

- Preparar o roteador (Raspberry Pi + Raspbian)

1. Instalar S.O.
  - https://www.raspberrypi.org/documentation/installation/installing-images/README.md
  
2. Configurar Wi-Fi
  - startx
  - executar comando "wpa-gui"

3. Executar "apt-get update" e "apt-get upgrade"
  
4. Executar
  - "apt-get install Mosquitto"
  - "apt-get install oracle-java7"
  - "apt-get install librxtx-java"
  - "apt-get install isc-dhcp-server"
  - "apt-get install hostapd"
  
5. Instalar Acess Point
  - https://learn.adafruit.com/setting-up-a-raspberry-pi-as-a-wifi-access-point/install-software
