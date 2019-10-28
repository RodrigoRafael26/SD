Para correr o multicast server:
	É necessário passar como argumento um config file com a seguinte estrutura:

	4320 (multicast port)
	224.0.224.0 (multicast address)
	6000 (tcp port)
	1 (serverID)
	
	Os multicast ports e address têm de ser iguais em todos os multicast, no entanto o serverID tem de ser diferente
	se os multicast correrem na mesma máquina o tcp port também deve ser diferente.


Para correr o RMI server:
	É necessário passar como parametro os seguintes argumentos:

	4320 (multicast port, arg0) 
	224.0.224.0 (multicast address, arg1)			

	As configs de multicast devem ser iguais às configurações dadas ao multicast

Para correr o RMI client:
	É necessário passar como parametro os seguintes argumentos:

	192.168.0.1 (IP onde o RMI server está a correr, arg0) 
	192.168.0.2 (IP onde o RMI client está a correr, arg1)	


Se houverem ficheiros guardados com informação previa eles devem ter o mesmo path do ficheiro executavel