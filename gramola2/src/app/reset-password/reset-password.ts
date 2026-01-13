import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms';   
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../user';

@Component({
  selector: 'app-reset-password',
  standalone: true,
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
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // esto lee el token de la barra de direcciones 
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.errorMsg = 'Error: No hay token. Usa el enlace de tu correo.';
      }
    });
  }

  cambiarPass() {
    // validamos que las contraseñas coincidan
    if (this.newPwd !== this.newPwd2) {
      this.errorMsg = 'Las contraseñas no coinciden.';
      return;
    }

    // preparamos los datos
    const body = {
      token: this.token,
      newPwd: this.newPwd
    };

    // llamamos al servicio para cambiar la contraseña
    this.userService.confirmarResetPassword(this.token, this.newPwd).subscribe({
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
