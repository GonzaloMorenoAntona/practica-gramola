
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../user'; 
import { SpotifyService } from '../spotify'; 
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink], 
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  loginForm: FormGroup;
  error?: string; 

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService, 
    private spotifyService: SpotifyService, 
    private router: Router 
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  onSubmit() {
    console.log('Valor del email:', this.loginForm.get('email')?.value);
    console.log('Valor de la contraseña:', this.loginForm.get('password')?.value);

    if (this.loginForm.valid) {
      const email = this.loginForm.get('email')?.value;
      const password = this.loginForm.get('password')?.value;

      // Llama al servicio para hacer login
      this.userService.login(email, password).subscribe({
        next: (response: any) => {

          // verificar que la respuesta tiene el clientId esperado
          const clientId = response.clientId; // Accede a response.clientId

          if (!clientId) {
            console.error('Error: El backend no devolvió un clientId válido en el JSON');
            this.error = 'Error en la respuesta del servidor';
            return;
          }

          sessionStorage.setItem("clientId", clientId);
          sessionStorage.setItem('barName', response.bar);
          sessionStorage.setItem('barEmail', email);


          this.userService.isLoggedIn.set(true); // Actualiza el estado de login  
          
          this.spotifyService.getToken(); // llamada al servicio para redirigir a Spotify
          //si todo va bien, redirige a la página de música
        },
        error: (err) => {
          console.error('Error en el login:', err);
          this.error = err.error?.message || 'Error en el login';
        }
      });
    } else {
      console.log('Formulario no válido');
      this.error = 'Por favor, rellena todos los campos correctamente.';
    }
  }
}
