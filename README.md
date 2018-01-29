# Vertx-mesh
Vertx-mesh is a service mesh implement that is based on Vert.x


# what' the service mesh mean?
![image](https://github.com/dinstone/vertx-mesh/wiki/images/mesh.png)

# How to start?
## start vertx-mesh
```
    java -Dconfig.file=config.json com.dinstone.mesh.vertx.ApplicationActivator
    java -Dconfig.file=config-7074.json com.dinstone.mesh.vertx.ApplicationActivator
```

## register service to vertx-mesh
create service 'magnet' and endpoint on localhost:8988, then register service to vertx-mesh by admin endpoint(localhost:8888/upline).
```
    curl --request GET --url 'http://localhost:8888/upline?service-name=magnet.online&service-port=8988&service-health=/info'
```

## invoke service 'magnet' by vertx-mesh
api pattern:
```
    curl --request GET --url 'http://localhost:7070/magnet.online/info'
```

proxy pattern:
```
    curl --request GET --url 'http://localhost:7474/info' --header 'host: magnet.online'
```


