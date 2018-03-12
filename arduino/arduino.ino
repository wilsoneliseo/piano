#include <String.h>
const byte LED_OUT_9 = 9;
const byte LED_OUT_13 = 13;
const byte DIVIDER = 255;/**< Bandera de mensaje. Se utiliza a modo de
        bandera para indicar el final de un mensaje de envio, o recepción, hacia app java*/

const byte JUEGO_FACIL = 0;
const byte JUEGO_MEDIO = 1;
const byte JUEGO_DIFICIL = 2;

byte input = 0;

boolean hayMensaje;

byte facil[] = {'F', 'a', 'c', 'i', 'l', DIVIDER};/**< Representa un
        paquete y su contenido el mensaje. La
        aplicacion de java tiene para recibir un
        mensaje de 1023 bytes por paquete.*/

byte medio[] = {'M', 'e', 'd', 'i', 'o', DIVIDER};/**< Representa un
        paquete y su contenido el mensaje. La
        aplicación de java tiene para recibir un
        mensaje de 1023 bytes por paquete.*/

const int SIZE_TEMP_BYTE = 1024;
byte tempBytes[SIZE_TEMP_BYTE];
int numTempBytes;


void setup() {
  Serial.begin(9600);
  pinMode(LED_OUT_9, OUTPUT);
  pinMode(LED_OUT_13, OUTPUT);
  digitalWrite(LED_OUT_13, LOW);

  for (int i = 0; i < SIZE_TEMP_BYTE; i++) tempBytes[SIZE_TEMP_BYTE] = 0;
  numTempBytes = 0;
  hayMensaje = false;
}

void enviarDatoJava(const String s) {
  const int len = s.length();
  byte buff[len + 1];
  s.getBytes(buff, len + 1);
  buff[len] = DIVIDER;
  Serial.write(buff, sizeof(buff));
}

void serialReader(const byte & _input) {
  if (_input < 0) {
    input += 256;
  }
  if (_input == DIVIDER) {
    if (numTempBytes > 0) {
      hayMensaje = true;
    }
  } else {
    tempBytes[numTempBytes] = _input;
    ++numTempBytes;
  }
}

void tratarMensajeRecibido() {
  if (numTempBytes == 1) {
    switch (tempBytes[0]) {
      case JUEGO_FACIL:
        enviarDatoJava("Juego facil");
        break;
      case JUEGO_MEDIO:
        enviarDatoJava("Juego medio");
        break;
      case JUEGO_DIFICIL:
        enviarDatoJava("El juego mas dificil");
        break;
      default:
        // if nothing else matches, do the default
        // default is optional
        break;
    }
  } else { // si el mensaje tiene mas de un byte se asume que se trata de las notas del archivo midi

  }
  numTempBytes = 0;
  hayMensaje = false;
}

void loop() {
  if (Serial.available() ) {

    input = Serial.read();
    serialReader(input);

    if (hayMensaje)
      tratarMensajeRecibido();
  }
}

