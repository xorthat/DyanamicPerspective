
function accel_plot_cube
interfaceObject = udp('127.0.0.1', 5555, 'LocalPort', 5555);
figureHandle = figure('CloseRequestFcn',{@localCloseFigure,interfaceObject});
axesHandle = axes('Parent',figureHandle, ...
    'ZGrid','on','YGrid','on', 'XGrid','on');
xlabel(axesHandle,'x');
ylabel(axesHandle,'y'); zlabel(axesHandle,'z');
axis([-10 10 -10 10 -10 10]);
vert = [-1 -4 -0.5;1 -4 -0.5;1 4 -0.5;-1 4 -0.5;...
    -1 -4 0.5;1 -4 0.5;1 4 0.5;-1 4 0.5];
fac = [1 2 6 5;2 3 7 6;3 4 8 7;4 1 5 8;1 2 3 4;5 6 7 8];
pat = patch('Vertices',vert,'Faces',fac,...
      'FaceVertexCData',hsv(6),'FaceColor','flat',...
      'Parent',axesHandle);
view(3)
axis vis3d
bytesToRead = 500;
interfaceObject.BytesAvailableFcn = {@localReadAndPlot,pat,bytesToRead};
interfaceObject.BytesAvailableFcnMode = 'byte';
interfaceObject.BytesAvailableFcnCount = bytesToRead;
fopen(interfaceObject);
pause(3);
snapnow;

function localReadAndPlot(interfaceObject,~,pat,bytesToRead)
    persistent vert vert0 flag;
    if isempty(vert0)
        flag = 1;
        vert0 = [-1 -4 -0.5;1 -4 -0.5;1 4 -0.5;-1 4 -0.5;...
    -1 -4 0.5;1 -4 0.5;1 4 0.5;-1 4 0.5];
        vert = vert0;
    end
    A = char(fread(interfaceObject,bytesToRead))';
    C = strsplit(A,',');
    data = str2double(C(3:end));
%     data = [data prev_data(numel(data)+1:end)];
    if ~isnan(data)
        if numel(data) >= 3
            %data(2)
            Accel = data(1:3);
            x = atan(Accel(1)/ sqrt(Accel(2)^2 + Accel(3)^2));
            y = atan(Accel(2)/ sqrt(Accel(1)^2 + Accel(3)^2));
            z = atan(sqrt(Accel(1)^2 + Accel(2)^2) / Accel(3));
            R = findRotationMatrx(pi-x,pi-y,pi-z);
            vert = (R*vert0')';
            flag = flag + 1;
        end
    end
    if flag == 5
        set(pat,'Vertices',vert); 
        drawnow; flag = 0;
    end
	

function localCloseFigure(figureHandle,~,interfaceObject)
fclose(interfaceObject);
delete(interfaceObject);
clear interfaceObject;
delete(figureHandle);

function R = findRotationMatrx(x,y,z)
    R = [cos(z) -sin(z) 0; sin(z) cos(z) 0;0 0 1]*...
        [cos(y) 0 sin(y); 0 1 0; -sin(y) 0 cos(y)]*...
        [ 1 0 0; 0 cos(x) -sin(x); 0 sin(x) cos(x)];
        
