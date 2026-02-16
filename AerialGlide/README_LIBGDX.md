# Aerial Glide migrado a LibGDX

## Estructura

- `core/`: gameplay, pantallas, audio y red compartida.
- `desktop/`: launcher de escritorio y modo servidor dedicado.
- `Resources/`: assets empaquetados dentro del JAR del módulo desktop.

## Ejecución

### Servidor dedicado (máquina 1)

```bash
gradle :desktop:run --args="--mode=server --port=5000"
```

### Servidor + cliente (host) (máquina 1)

```bash
gradle :desktop:run
```

En menú presionar `2` (host online).

### Cliente (máquina 2 o 3)

```bash
gradle :desktop:run
```

En menú escribir IP local del servidor (`192.168.x.x`) con teclado, luego presionar `3`.

## Exportación JAR

```bash
gradle :desktop:jar
```

Salida:

- `desktop/build/libs/aerial-glide-desktop.jar`

Ejemplos de ejecución:

```bash
java -jar aerial-glide-desktop.jar --mode=server --port=5000
java -jar aerial-glide-desktop.jar
```

## Lógica de red

- Servidor autoritativo TCP: `core/src/main/java/com/aerialglide/net/TcpGameServer.java`
- Cliente TCP: `core/src/main/java/com/aerialglide/net/TcpGameClient.java`
- Parseo de estado sincronizado: `core/src/main/java/com/aerialglide/net/ServerState.java`
