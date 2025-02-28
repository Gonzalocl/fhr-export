Entre llamadas a subfolders se hacen siempre las mismas llamadas solo cambia el id.
EmployeeFolders es siempre la misma excepto bodies/root_request_bodies/POST_https:--api.factorialhr.com-graphql_EmployeeFolders_1.json y la unica diferencia es [].
Cada operation name todos coinciden en tener la misma query

Por variables son diferentes:
2 EmployeeFolders: con y sin []
2 Documents: la que se hace en root y la que se hace en folder
3 Folders: las dos que se hacen en los folders y la que se hace en el root

Pasos:
1. Llamar a 2xEmployeeFolders
2. Llamar a Documents y Folders en root
3. Llamar a otros endpoints de info ()
4. Para cada folder llamar a
4.1. Documents y 2xFolders

Hacer un mapa de folders ya revisados y no revisados para no repetir

Revisar en las queries que no haya ningun id.

Check id repetidos de folders y docs

Check file not exits
