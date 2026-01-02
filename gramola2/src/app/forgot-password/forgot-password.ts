import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [FormsModule, CommonModule], // Importante para usar ngModel y *ngIf
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  email: string = '';
  mensaje: string = '';
  errorMsg: string = '';

  constructor(private http: HttpClient) {}

  pedirRecuperacion() {
    this.mensaje = '';
    this.errorMsg = '';

    const body = { email: this.email };

    // responseType: 'text' es vital porque el backend devuelve un String (la URL), no un JSON
    this.http.post('http://127.0.0.1:8080/users/request-reset-pwd', body, { responseType: 'text' })
      .subscribe({
        next: (respuesta) => {
          // El backend nos devuelve la URL simulada
          this.mensaje = 'Correo enviado. Copia este enlace en tu navegador: ' + respuesta;
        },
        error: (err) => {
          this.errorMsg = 'Error: El correo no existe o hubo un fallo.';
          console.error(err);
        }
      });
  }
}
