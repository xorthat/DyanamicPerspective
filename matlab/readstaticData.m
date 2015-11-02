% sample program to read the data from static dataset in dataset folder
% data format 13 columns
% col 1 - system time stamp
% col 2, 6, 10 - sensor id
%       3 - accelometer
%       4 - gyroscope
%       5 - magnetometer
% col (3,4,5), (7,8,9), (11,12,13) - data triplets along x,y,z axis for
% corresponding sensors


ax = dlmread('../dataset/accel_x.txt');
figure, hold on;
plot(ax(:,1),ax(:,3),'r');  %ax
plot(ax(:,1),ax(:,4),'g');  %ay
plot(ax(:,1),ax(:,5),'b');  %az

plot(ax(:,1),ax(:,7),'y');  %gx
plot(ax(:,1),ax(:,8),'m');  %gy
plot(ax(:,1),ax(:,9),'c');  %gz