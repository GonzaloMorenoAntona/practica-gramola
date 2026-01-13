import { Injectable,signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  private baseUrl: string = 'http://127.0.0.1:8080';

  showNavbar = signal<boolean>(true);
  isLoggedIn = signal<boolean>(!!sessionStorage.getItem('clientId'));

  constructor(private http: HttpClient) {}

   register(bar: string, email: string, pwd1: string, pwd2: string, clientId: string, clientSecret: string): Observable<any> {
    const body = { bar, email, pwd1, pwd2, clientId, clientSecret };
    return this.http.post(`${this.baseUrl}/users/register`, body);
  }
  login(email: string, pwd: string): Observable<any> { 
    const body = { email, pwd }; 
    return this.http.post<any>(`${this.baseUrl}/users/login`, body, {
      withCredentials: true 
    });
  }
  logout() {
    sessionStorage.clear();
    this.isLoggedIn.set(false);
  }
  requestResetPassword(email: string) {
    const body = { email: email };
    return this.http.post(`${this.baseUrl}/users/request-reset-pwd`, body, { responseType: 'text' });
  }

  // Método para cambiar la contraseña usando el token recibido por email
  confirmarResetPassword(token: string, newPwd: string) {
    const body = { token: token, newPwd: newPwd };
    return this.http.post(`${this.baseUrl}/users/reset-pwd`, body);
  }
}



