u = udp('127.0.0.1', 5555, 'LocalPort', 5555);
fopen(u);
%%
% to check the status in Linux $ ss -ap | grep 5555
while 1
    A = char(fread(u,10))';
    C = strsplit(A,',');
    data = str2double(C(2:end))
end

%%
fclose(u);