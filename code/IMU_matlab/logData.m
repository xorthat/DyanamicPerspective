function logData(obj, name)
[av, tav] = angvellog(obj);
[ac, tac] = accellog(obj);
[mag, tmag] = magfieldlog(obj);
[o, to] = orientlog(obj);
save([name '.mat'], 'av', 'tav', 'ac', 'tac', 'mag', 'tmag', 'o', 'to');
