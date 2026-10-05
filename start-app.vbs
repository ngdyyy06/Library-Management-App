Set WshShell = CreateObject("WScript.Shell")

scriptPath = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)

WshShell.Run """" & scriptPath & "\start-app.bat""", 0, False