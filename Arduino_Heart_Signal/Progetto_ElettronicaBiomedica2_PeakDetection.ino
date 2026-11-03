//Inclusione Librerie Utilizzate
#include <CircularBuffer.h>                                                            //includiamo la libreria dei buffer circolari per permetterci di salv are i dati 
#include <Wire.h>
#include <Adafruit_GFX.h>                                                              //librerie utili all'inizializzazione del display oled
#include <Adafruit_SSD1306.h>   


//Inizializzazioni delle variabili utili alla visualizzazione sul display OLED
#define SCREEN_WIDTH 128                                                               //ampiezza display oled in pixels
#define SCREEN_HEIGHT 32                                                               //altezza display oled in pixels
#define OLED_RESET     4                                                               //pin di reset del display oled
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
const int WIDTH = 128;
const int HEIGHT = 32;
const int LENGTH = WIDTH;
int x;                                                                                 //variabile d'appoggio per popolare lo schermo oled in altezza 
int y[LENGTH];                                                                         //variabile d'appoggio per popolare lo schermo oled in lunghezza


void clearY(){                                                                         //void che ci permette di ripulire lo schermo dopo il riempimento
  for (int i = 0; i < LENGTH; i++){
    y[i] = -1;
  }
}
 

void drawY(){                                                                          //void che ci permette di scrivere lo schermo oled durante il suo riempimento
  display.drawPixel(0, y[0], WHITE);
  for (int i = 1; i < LENGTH; i++){
    if (y[i] != -1){
      display.drawLine(i - 1, y[i - 1], i, y[i], WHITE);
    }else{
      break;
    }
  }
}


//Inizializzazioni Variabili Sensore AD8232
int sensorPin = A0;                                                                    //pin output sensore AD8232
volatile int sensorValue = 0;                                                          //variabile in cui allocare output sensore AD8232


//Variabili Filtri (Passa Basso 15 Hz e Passa Alto 5 Hz)
float EMA_a_low = 0.6607;                                                              //coefficiente alpha del filtro passa-basso
float EMA_a_high = 0.4143;                                                             //coefficiente alpha del filtro passa-alto
int EMA_S_low = 0;                                                                     //inizializza il segnale filtrato per il Passa Basso
int EMA_S_high = 0;                                                                    //inizializza il segnale filtrato per il Passa Alto
int highpass = 0;                                                                      //variabile in cui allocare output filtrato con filtro passa-alto
int bandpass = 0;                                                                      //variabile in cui allocare output filtrato con filtro passa-banda


//Varibaili Filtro Derivativo e Quadratura 
float deriv = {0.0};                                                                   //uscita del filtro derivativo 
int t_start = 0;                                                                       //variabile d'appoggio per salvare l'istante temporale corrente per calcolare la derivata
int t_previous = 0;                                                                    //variabile d'appoggio per salvare l'istante temporale precedente per calcolare la derivata
float interval = 0;                                                                    //delta t per il filtro 
float previous_value = 0;                                                              //variabile d'appoggio per aggiornare il valore dopo il calcolo della derivata
float quadratic = 0;                                                                   //variabile di appoggio per la quadratura 


//Variabili Filtro a Media Mobile                                                   
const int  FC = 2*10^3;                                                                //frequenza di campionamento stimata tramite calcolo
const int N_AVG = floor(FC/10);                                                        //prendiamo la FC/10 per avere un numero di campioni tali da coprire 100 ms, durata della finestra QRS
CircularBuffer<int, N_AVG> buffer_mediamobile;                                         //inizializziamo il buffer circolare per l'applicazione del filtro a media mobile
boolean isFull_mediamobile = false;                                                    //inizializzazione variabile booleana per il controllo del riempimento del buffer_mediamobile


//Variabili Sogliatura
const int  larghezza_buffer = 2*FC;                                                    //inizializzazione ampiezza del buffer su cui trovare la soglia adattiva
boolean isFull_sogliatura = false;                                                     //inizializzazione variabile booleana per il controllo del riempimento del buffer_sogliatura
CircularBuffer<int, larghezza_buffer> buffer_sogliatura;                               //inizializziamo il buffer circolare per trovare la soglia adattiva
float peakTime = 0;                                                                    //inizializziamo la variabile d'appoggio per salvare il picco trovato
const int tempo_refreshthreshold = 1500;                                               //inizializziamo il tempo di refresh, cioè 1500[ms] se non troviamo un picco in quel tempo allora la soglia si adatta
const int tempo_refrattario = 250;                                                     //inizializziamo il periodo refrattario in cui non ci può essere un altro picco R, cioè 250[ms]
boolean isThresholdInit = false;                                                       //inizializzazione variabile booleana per il controllo della soglia
float sig_level;                                                                       //variabile d'appoggio per stabilire il livello del segnale da considerare
float noise_level;                                                                     //variabile d'appoggio per stabilire il livello del rumore da evitare
float threshold;                                                                       //variabile d'appoggio per aggiornare la soglia


//Inizializzazioni Variabili Rilevazione Picco
boolean isFull_RR = false;                                                             //inizializzazione variabile booleana per il controllo del riempimento del buffer_RR
unsigned long prevPeakTime;                                                            //inizializziamo la variabile d'appoggio per salvare il picco precedente trovato
unsigned long R_R;                                                                     //inizializziamo la variabile d'appoggio per salvare l'HRV, distanza temporale tra due picchi R
float bpm=0.0;                                                                         //inizializziamo la variabile d'appoggio per il calcolo del BPM


//Inizializzazioni Variabili Calcolo BPM medio
CircularBuffer<int, 5> buffer_avgbpm;                                                  //inizializziamo il buffer circolare per trovare il BPM medio
boolean isFull_avgbpm = false;                                                         //inizializzazione variabile booleana per il controllo del riempimento del buffer_avgbpm
float sum_avgbpm=0.0;                                                                  //inizializziamo la variabile d'appoggio per il calcolo del BPM medio
float avg_bpm=0.0;                                                                     //inizializziamo la variabile d'appoggio per il salvare il valore del BPM medio calcolato


//Variabili Calcolo Frequenza di Campionamento
//unsigned int tprimis;                                                                //variabile d'appoggio inizio tempo di processamento
//unsigned int tfinis;                                                                 //variabile d'appoggio di fine tempo di processamento
//unsigned int ttrans;                                                                 //variabile d'appoggio differenza tra i tempi di processamento per ricavare la frequenza di campionamento di Arduino




void setup(){
  
  Serial.begin(115200);                                                                //set-up Seriale 115200 bits/second
  
  EMA_S_low = analogRead(sensorPin);                                                   //set-up EMA_S_low per t=1
  EMA_S_high = analogRead(sensorPin);                                                  //set-up EMA_S_high per t=1
 if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)){                                       //set-up display oled (Address 0x3C for 128x32)
    Serial.println(F("SSD1306 allocation failed"));
    for(;;); 
  }
  display.display();
  display.clearDisplay();  
  x = 0;                                                                               //impostiamo il display oled vuoto e partiamo da x=0 per il riempimento 
  clearY();                                                                            //operazione per svuotare il display oled
}




//Loop Esecuzione
void loop(){                                                                           //inizio loop


//tprimis=micros();                                                                    //facciamo partire la stima della frequenza di campionamento

sensorValue = analogRead(sensorPin);                                                   //prelevo segnale ECG dal sensore AD8232
t_start = millis();                                                                    //tempo prelievo segnale 

y[x] = map(sensorValue, 0, 1023, HEIGHT + 2, 0);                                       //mappiamo i valori del sensore sui valori del display oled
drawY();                                                                               //riempiamo il display con l'ecg prelevato dal sensore
x++;                                                                                   //riempiamo in lunghezza, lungo lo scorrere della x
  if (x >= WIDTH){                                                                     //se la x supera la lunghezza massima del display, svuota il display e riparti da x=0
  x=0;
  display.clearDisplay();
  clearY();
  }
display.display();
delay(10);                             


//sensorValue = analogRead(sensorPin);                                                   //prelevo segnale ECG dal sensore AD8232
//t_start = millis();                                                                    //tempo prelievo segnale 


//Fase Filtraggio LP-HP del Segnale Prelevato dal Sensore
EMA_S_low = (EMA_a_low*sensorValue) + ((1-EMA_a_low)*EMA_S_low);                       //calcolo EMA_S_low per i vari istanti t del loop
EMA_S_high = (EMA_a_high*sensorValue) + ((1-EMA_a_high)*EMA_S_high);                   //calcolo EMA_S_high per i vari istanti t del loop
highpass = sensorValue - EMA_S_low;                                                    //trovo l'output del filtraggio passa-alto
bandpass = EMA_S_high - EMA_S_low;                                                     //trovo l'output del filtraggio passa-banda  


//Filtraggio Derivativo
interval = t_start - t_previous;                                                       //calcolo delta t
t_previous = t_start;                                                                  //aggiornamento tempo 
deriv = ( bandpass - previous_value ) / interval;                                      //implementazione filtro derivativo 
previous_value = bandpass;                                                             //aggiornamento valore                                


//Quadratura
quadratic=deriv*deriv;                                                                 //trovo l'output del quadratore
isFull_mediamobile = not(buffer_mediamobile.unshift(quadratic));                       //aggiungiamo elemento e vediamo se buffer_mediamobile è pieno


//Filtraggio a Media Mobile
  if(isFull_mediamobile){                                                              //se isFull_mediamobile è pieno allora vuol dire che siamo pronti per inizializzare
  float integ_sig = 0;                                                                 // 
    for (int i=0; i<N_AVG;i++){                                                        //
    integ_sig += buffer_mediamobile[i];                                                //
    }
  integ_sig = integ_sig/N_AVG;                                                         //
  isFull_sogliatura = not(buffer_sogliatura.unshift(integ_sig));                       //aggiungiamo elemento e vediamo se buffer_sogliatura è pieno
  float sincePeak = millis() - peakTime;                                               //sincePeak ci dice quanto tempo è passato dall'ultimo picco rilevato
  


//Soglia Adattiva
  if (isFull_sogliatura){                                                              //se isFull_sogliatura è pieno allora vuol dire che siamo pronti per inizializzare la soglia e quindi poi per rilevare i picchi
    if ((not(isThresholdInit)) || (sincePeak > tempo_refreshthreshold)){               //
    float noise_sig = 0;                                                               //facciamo la media sul segnale integrato
    float max_sig = 0;                                                                 //troviamo il massimo sul segnale integrato
      for (int i=0; i<larghezza_buffer;i++){                                           //
      noise_sig += buffer_sogliatura[i];                                               //
        if (buffer_sogliatura[i] > max_sig){                                           //
          max_sig = buffer_sogliatura[i];                                              //
        }
      }
    sig_level = max_sig;                                                               //
    noise_level = noise_sig/larghezza_buffer;                                          //          
    threshold = noise_level + 0.25*(sig_level-noise_level);                            //
    isThresholdInit = true;                                                            //
    }   
  if (isThresholdInit && sincePeak > tempo_refrattario){                               //se sono passati 250ms allora controlla il nuovo picco (controllo aggiuntivo)
    if (integ_sig > threshold){                                                        //se il segnale filtrato supera la soglia rileva il picco R, la soglia è adattiva e varia ad ogni ciclo cardiaco
    sig_level = 0.125*integ_sig + 7*0.125*sig_level;                                   //
    noise_level = 0.125*integ_sig + 7*0.125*noise_level;                               //
    threshold = noise_level + 0.25*(sig_level-noise_level);                            //la soglia si aggiorna sulla base dei valori riportati sul paper scritto da PanTompkins
    prevPeakTime = peakTime;                                                           //prevPeakTime si aggiorna ogni volta che trovo un nuovo picco
    peakTime = millis();                                                               //
    R_R = (peakTime - prevPeakTime);                                                   //calcolo l'HRV sulla base della distanza temporale tra il picco precedente e il picco corrente
    bpm=60000/R_R;                                                                     //formula per ricavare il valore del BPM
    //Serial.print(R_R);                                                               //se volessi stampare l'HRV solo quando trova il picco R
    //Serial.print("; ");


    isFull_avgbpm = not(buffer_avgbpm.unshift(bpm));                                   //aggiungiamo elemento e vediamo se buffer_sogliatura è pieno
      if (isFull_avgbpm){                                                              //se il buffer_avgbpm è pieno allora procedi con il calcolo del BPM medio
        for (int i=0; i<5;i++){                                                        
        sum_avgbpm += buffer_avgbpm[i];                                                //calcolo della somma dei BPM
        }
      sum_avgbpm=sum_avgbpm/5;                                                         //calcolo della media dei valori di BPM
      }
    }
  }    
}




//Print Seriali e sul Display Oled
  Serial.println(sensorValue);                                                           //print seriale segnale
  //Serial.print("; ");
  //Serial.print(bandpass);                                                              //print seriale segnale filtarto con filtro passa banda
  //Serial.print("; ");
  //Serial.print(deriv);                                                                 //print seriale segnale derivato
  //Serial.print("; ");
  //Serial.print(quadratic);                                                             //print seriale segnale quadratico
  //Serial.print("; ");
  //Serial.print(integ_sig);                                                             //print seriale segnale integrato con finestra a media mobile
  //Serial.print("; ");
  //Serial.print(threshold);                                                             //print seriale della soglia adattiva
  //Serial.print("; ");
  //Serial.print(R_R);                                                                   //print seriale dell'HRV
  //Serial.print("; ");
  //Serial.print(bpm);                                                                   //print seriale del valore BPM
  //Serial.print("; ");
  //Serial.println(sum_avgbpm);                                                          //print seriale del valore BPM medio


  display.clearDisplay();                                                              //ripulisci il display
  display.setTextSize(1);                                                              //set-up dimensione testo
  display.setTextColor(WHITE);                                                         //set-up colore testo
  display.setCursor(0,0);                                                              //set-up cursore a 0
  display.println("BPM");                                                              //scritta fissa BPM
  display.setCursor(30,0);                                                             //set-up cursore a 30 lungo x
  display.println(sum_avgbpm);                                                         //print sul display del valore del BPM medio che varia
  display.display();
  delay(20);    

  }
//tfinis=micros();                                                                     //da queste istruzioni mi ricavo infine il tempo di campionamento da cui ricavo la FC usata
//ttrans=tfinis-tprimis;
//Serial.println(ttrans);
//delay(20);  
}                                                                                      //fine loop
    
