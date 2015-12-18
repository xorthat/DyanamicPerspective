# -------------------------------------------------------
import socket, traceback

host = ''
port = 5555

s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
s.bind((host, port))

f = open('gyro_z.txt', 'w+')
while 1:
	try:
		message, address = s.recvfrom(8192)
		f.write(message)
		f.write("\n")
	except (KeyboardInterrupt, SystemExit):
		raise
	except:
		traceback.print_exc()
# -------------------------------------------------------
f.close()
