u = udp('127.0.0.1', 5555, 'LocalPort', 5555);
fopen(u);
%%
while 1
    A = char(fread(u,10))'
end