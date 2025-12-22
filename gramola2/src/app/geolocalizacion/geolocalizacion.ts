import { Component } from '@angular/core';

@Component({
  selector: 'app-geolocalizacion',
  imports: [],
  templateUrl: './geolocalizacion.html',
  styleUrl: './geolocalizacion.css'
})
export class Geolocalizacion {
  coordenadas? : GeolocationPosition
  ciudad? : string
  constructor(){
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.coordenadas = position;
        console.log(`Latitud: ${this.coordenadas.coords.latitude}, Longitud: ${this.coordenadas.coords.longitude}`);
        this.obtenerCiudad();
        this.obtenerClima();


        navigator.geolocation.watchPosition((newPosition) => {
          this.coordenadas = position;
          console.log(`Nueva posición - Latitud: ${this.coordenadas.coords.latitude}, Longitud: ${this.coordenadas.coords.longitude}`);
          
        },
        (error) => {
          console.error('Error al obtener la posición:', error);
        },
        { enableHighAccuracy: true, maximumAge: 0, timeout: 5000 }
      );
      });
    }
  }
  obtenerClima() {
    throw new Error('Method not implemented.');
  }


  private obtenerCiudad() {
    if (this.coordenadas) {
      fetch(`https://nominatim.openstreetmap.org/reverse?lat=${this.coordenadas.coords.latitude}&lon=${this.coordenadas.coords.longitude}&format=json`)
        .then(response => response.json())
        .then(data => {
          const city = data.address.city || data.address.town || data.address.village || 'Desconocida';
          console.log(`Ciudad: ${city}`);
        })
        .catch(error => {
          console.error('Error al obtener la ciudad:', error);
        });
    }

  }

}
