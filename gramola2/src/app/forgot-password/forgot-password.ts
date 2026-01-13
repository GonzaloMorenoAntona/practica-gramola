import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../user';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink], 
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  email: string = '';
  mensaje: string = '';
  errorMsg: string = '';

  constructor(private userService: UserService) {}

  pedirRecuperacion() {
    this.mensaje = '';
    this.errorMsg = '';

    const body = { email: this.email };

    // llamamos al servicio para pedir la recuperación
    this.userService.requestResetPassword(this.email)
      .subscribe({
        next: (respuesta) => {
          this.mensaje = 'Hemos enviado un enlace. ¡Revisa tu bandeja de entrada!';
        },
        error: (err) => {
          this.errorMsg = 'Error: El correo no existe o hubo un fallo.';
          console.error(err);
        }
      });
  }
}
