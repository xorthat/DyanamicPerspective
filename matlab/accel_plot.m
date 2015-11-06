
function accel_plot
interfaceObject = udp('127.0.0.1', 5555, 'LocalPort', 5555);
figureHandle = figure('NumberTitle','off',...
    'Name','Live Data Stream Plot',...
    'CloseRequestFcn',{@localCloseFigure,interfaceObject});
axesHandle = axes('Parent',figureHandle,...
    'YGrid','on',...
    'XGrid','on');
xlabel(axesHandle,'Number of Samples');
ylabel(axesHandle,'Value');
hold on; axis([0 100 -180 180]);
ax = plot(axesHandle,0,'-r','LineWidth',1);
ay = plot(axesHandle,0,'-g','LineWidth',1);
az = plot(axesHandle,0,'-b','LineWidth',1);
bytesToRead = 500;
interfaceObject.BytesAvailableFcn = {@localReadAndPlot,ax, ay, az,bytesToRead};
interfaceObject.BytesAvailableFcnMode = 'byte';
interfaceObject.BytesAvailableFcnCount = bytesToRead;
fopen(interfaceObject);
pause(3);
snapnow;

function localReadAndPlot(interfaceObject,~,ax,ay,az,bytesToRead)
    persistent Ax flag fdata;
    if isempty(Ax)
       Ax = zeros(3,100);
       flag = 1; fdata = zeros(1,13);
    end
    A = char(fread(interfaceObject,bytesToRead))';
    C = strsplit(A,',');
    data = str2double(C(3:end));
%     data = [data prev_data(numel(data)+1:end)];
    if ~isnan(data)
        if numel(data) >= 3
            %data(2)
            Accel = data(1:3);
            theta = atan2d(Accel(1), sqrt(Accel(2)^2 + Accel(3)^2));
            roll = atan2d(Accel(2), sqrt(Accel(1)^2 + Accel(3)^2));
            beta = atan2d(sqrt(Accel(1)^2 + Accel(2)^2) , Accel(3));
            Ax = [Ax(:,2:end), [theta roll beta]']; %fdata(1:3)'];
            %fdata = data;
        end
    end
    flag = flag + 1;
    if flag == 5
        set(ax,'Ydata',Ax(1,:)); set(ay,'Ydata',Ax(2,:)); set(az,'Ydata',Ax(3,:)); 
        drawnow; flag = 0;
    end

function localCloseFigure(figureHandle,~,interfaceObject)
fclose(interfaceObject);
delete(interfaceObject);
clear interfaceObject;
delete(figureHandle);
