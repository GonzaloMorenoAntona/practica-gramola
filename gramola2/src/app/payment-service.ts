import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  constructor(private client: HttpClient) { }

  // metodo para pedir la lista de precios al servidor
  getPlans(): Observable<any[]> {
    return this.client.get<any[]>('http://127.0.0.1:8080/payments/plans', { 
      withCredentials: true 
    });
  }

  // ahora recibe el plan y usa POST para enviarlo
  prepay(selectedPlanId: number, token: string): Observable<any> {
    // creamos el paquetito con el ID para enviarlo al backend
    const body = { priceId: selectedPlanId, token: token };
    return this.client.post('http://127.0.0.1:8080/payments/prepay', body, {
      withCredentials: true,
      observe: 'response',
      responseType: 'text' 
    });
  } 

 // metodo para confirmar el pago
  confirm(transactionId: string, token: string): Observable<any> {
    const body = { transactionId, token };
    return this.client.post('http://127.0.0.1:8080/payments/confirm', body, {
      withCredentials: true,
      observe: 'response'
    });
  }
  // metodo para pedir el precio de una canción al servidor
  getSongPrice(): Observable<any> {
    return this.client.get('http://127.0.0.1:8080/payments/song-price', { 
      withCredentials: true 
    });
  }
}
