import { Injectable,signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  private apiUrlRegister = 'http://localhost:8080/users/register';
  private apiUrlLogin = 'http://localhost:8080/users/login';

  showNavbar = signal<boolean>(true);
  isLoggedIn = signal<boolean>(!!sessionStorage.getItem('clientId'));

  constructor(private http: HttpClient) {}

   register(bar: string, email: string, pwd1: string, pwd2: string, clientId: string, clientSecret: string): Observable<any> {
    const body = { bar, email, pwd1, pwd2, clientId, clientSecret };
    return this.http.post(this.apiUrlRegister, body);
  }
  login(email: string, pwd: string): Observable<any> { // Devuelve Observable<any> para manejar la respuesta
    const body = { email, pwd }; // El cuerpo de la petición
    return this.http.post<any>(this.apiUrlLogin, body, {
      withCredentials: true // Si usas sesiones HTTP
    });
  }
  logout() {
    sessionStorage.clear();
    this.isLoggedIn.set(false);
  }
}



