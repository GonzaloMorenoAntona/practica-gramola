import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // <--- Importante para *ngIf
import { FormsModule } from '@angular/forms';   // <--- Importante para [(ngModel)]
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  // OJO AQUÍ: Tienen que estar estos dos imports para que funcionen los "ng"
  imports: [CommonModule, FormsModule], 
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPassword implements OnInit {
  token: string = '';
  newPwd: string = '';
  newPwd2: string = '';
  errorMsg: string = '';
  mensaje: string = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Esto lee el token de la barra de direcciones (?token=XYZ)
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.errorMsg = 'Error: No hay token. Usa el enlace de tu correo.';
      }
    });
  }

  cambiarPass() {
    // 1. Validamos que las contraseñas coincidan
    if (this.newPwd !== this.newPwd2) {
      this.errorMsg = 'Las contraseñas no coinciden.';
      return;
    }

    // 2. Preparamos los datos
    const body = {
      token: this.token,
      newPwd: this.newPwd
    };

    // 3. Enviamos al backend
    this.http.post('http://localhost:8080/users/reset-pwd', body).subscribe({
      next: () => {
        alert('¡Contraseña cambiada! Ahora entra con tu nueva clave.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.errorMsg = 'Error: El enlace ha caducado o no es válido.';
        console.error(err);
      }
    });
  }
}
