import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  constructor(private client: HttpClient) { }

  // 1. NUEVO: Método para pedir la lista de precios al servidor
  getPlans(): Observable<any[]> {
    return this.client.get<any[]>('http://localhost:8080/payments/plans', { 
      withCredentials: true 
    });
  }

  // 2. MODIFICADO: Ahora recibe el ID (selectedPlanId) y usa POST para enviarlo
  prepay(selectedPlanId: number, token: string): Observable<any> {
    // Creamos el paquetito con el ID para enviarlo al backend
    const body = { priceId: selectedPlanId, token: token };

    // Cambiamos .get por .post porque estamos enviando datos (el body)
    return this.client.post('http://localhost:8080/payments/prepay', body, {
      withCredentials: true,
      observe: 'response',
      responseType: 'text' // Mantenemos esto IGUAL que lo tenías para que no falle
    });
  } 

  // 3. ESTE SE QUEDA EXACTAMENTE IGUAL QUE LO TENÍAS
  confirm(transactionId: string, token: string): Observable<any> {
    const body = { transactionId, token };
    return this.client.post('http://localhost:8080/payments/confirm', body, {
      withCredentials: true,
      observe: 'response'
    });
  }
  getSongPrice(): Observable<any> {
    return this.client.get('http://localhost:8080/payments/song-price', { 
      withCredentials: true 
    });
  }
}
