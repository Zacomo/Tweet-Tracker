

# Tweet Tracker
[![Language](https://img.shields.io/badge/Language-Java-brightgreen)](https://www.java.com/) <br>
[![API](https://img.shields.io/badge/Platform-JavaFX-red)](https://openjfx.io/) <br>
[![Platform](https://img.shields.io/badge/API-Twitter-blue)](https://developer.twitter.com/en)

Il progetto consiste nella realizzazione di un programma che sfrutti le API del social network Twitter. 
Tale progetto ha come obiettivo quello di mostrare le potenzialità degli strumenti messi a disposizione da Twitter per la raccolta di dati dalla sua piattaforma. 
I tweet possono essere un mezzo tramite la quale è possibile ottenere informazioni da eventi di vario tipo; se geolocalizzati acquisiscono ulteriore importanza perché forniscono un riscontro degli avvenimenti da una posizione precisa.

## Utilizzo programma
Il programma è stato scritto con openJDK14. <br>
Per il suo funzionamento è necessario importare le seguenti librerie:
- [JavaFX 15](https://gluonhq.com/products/javafx/)
- [Twitter4J](http://twitter4j.org/)
- [GSON](https://github.com/google/gson)
<br>
Le librerie di Google Maps e AnyChart sono già importate nei rispettivi file html. 
<br><br>
Inoltre è necessario inserire le proprie chiavi di Twitter nel file keys.json e la propria chiave di Google Maps nel file map.html (all'interno del campo source nel tag script).
<br><br>
Il file covidSample.json contiene dei tweet riguardanti il Covid-19 ritrovati tramite Tweet Tracker nel periodo tra il 20 e il 26 maggio 2020. 
È possibile caricare quel file nel programma tramite il pulsante "Carica JSON" e consultare i tweet al suo interno.
<br><br>
Per ulteriori informazioni sul funzionamento di Tweet Tracker, è possibile consultare il documento "tesi.pdf" presente in questo repository.
