
function real_time_data_stream_plotting
interfaceObject = udp('127.0.0.1', 5555, 'LocalPort', 5555);
figureHandle = figure('NumberTitle','off',...
    'Name','Live Data Stream Plot',...
    'CloseRequestFcn',{@localCloseFigure,interfaceObject});
axesHandle = axes('Parent',figureHandle,...
    'YGrid','on',...
    'XGrid','on');
xlabel(axesHandle,'Number of Samples');
ylabel(axesHandle,'Value');
hold on; axis([0 100 -20 20]);
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
    persistent Ax Ay Az flag prev_data;
    if isempty(Ax) && isempty(Ay) && isempty(Az)
       Ax = zeros(1,100); Ay = zeros(1,100); Az = zeros(1,100);
       flag = 1; prev_data = zeros(1,13);
    end
    A = char(fread(interfaceObject,bytesToRead))';
    C = strsplit(A,',');
    data = str2double(C(3:end));
    data = [data prev_data(numel(data)+1:end)];
    if ~isnan(data)
        Ax = [Ax(2:end), data(1)];
        Ay = [Ay(2:end), data(2)];
        Az = [Az(2:end), data(3)];
    end
    prev_data = data;
    flag = flag + 1;
    if flag == 5
        set(ax,'Ydata',Ax); set(ay,'Ydata',Ay); set(az,'Ydata',Az); 
        drawnow; flag = 0;
    end

function localCloseFigure(figureHandle,~,interfaceObject)
fclose(interfaceObject);
delete(interfaceObject);
clear interfaceObject;
delete(figureHandle);
