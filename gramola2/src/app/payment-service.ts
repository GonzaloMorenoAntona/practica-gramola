import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  constructor(private client: HttpClient) { }

  prepay(): Observable<any> {
    return this.client.get('http://localhost:8080/payments/prepay', {
      withCredentials: true,
      observe: 'response',
      responseType: 'text'  
    });
  } 
  confirm(transactionId: string, token: string): Observable<any> {
  const body = { transactionId, token };
  return this.client.post('http://localhost:8080/payments/confirm', body, {
    withCredentials: true,
    observe: 'response'
  });
}
}
