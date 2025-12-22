
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../user'; // Asegúrate de la ruta
import { SpotifyService } from '../spotify'; // Importa el servicio

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule], // Asegúrate de tener ReactiveFormsModule
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  loginForm: FormGroup;
  error?: string; // Para mostrar mensajes de error

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService, // Inyecta el servicio de usuario
    private spotifyService: SpotifyService, // ✅ Inyecta el servicio de Spotify
    private router: Router // Inyecta el router
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  onSubmit() {
    console.log("Botón de login pulsado");
    console.log('Valor del email:', this.loginForm.get('email')?.value);
    console.log('Valor de la contraseña:', this.loginForm.get('password')?.value);

    if (this.loginForm.valid) {
      const email = this.loginForm.get('email')?.value;
      const password = this.loginForm.get('password')?.value;

      // Llama al servicio para hacer login
      this.userService.login(email, password).subscribe({
        next: (response: any) => {

          // 1. Verificar que la respuesta tiene el clientId (ahora es un JSON)
          const clientId = response.clientId; // Accede a response.clientId

          if (!clientId) {
            console.error('Error: El backend no devolvió un clientId válido en el JSON');
            this.error = 'Error en la respuesta del servidor';
            return;
          }

          // 2. Guardar el clientId en sessionStorage
          sessionStorage.setItem("clientId", clientId);

          // 3. ✅ LLAMAR AL SERVICIO DE SPOTIFY PARA INICIAR EL FLUJO DE AUTENTICACIÓN
          // Esto construirá la URL y redirigirá el navegador a Spotify
          this.spotifyService.getToken(); // <-- Llamada al servicio

          // NOTA: No se ejecuta nada aquí después de getToken() si la redirección es exitosa
          // porque el navegador cambia de página.
        },
        error: (err) => {
          console.error('Error en el login:', err);
          // Muestra un mensaje de error al usuario
          this.error = err.error?.message || 'Error en el login';
        }
      });
    } else {
      console.log('Formulario no válido');
      // Opcional: Mostrar mensajes de error del formulario
      this.error = 'Por favor, rellena todos los campos correctamente.';
    }
  }
}
