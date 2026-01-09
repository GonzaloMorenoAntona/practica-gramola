import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 
import { UserService } from '../user';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  bar?: string;
  email?: string;
  pwd1?: string;
  pwd2?: string;
  clientId?: string;
  clientSecret?: string;
  
  registroOK : boolean = false;
  registroKO : boolean = false;
  
  // 1. AÑADIMOS ESTA VARIABLE PARA GUARDAR EL TEXTO DEL ERROR
  errorMessage: string = "";

  constructor(private service : UserService) { }

  registrar() {
    // Reseteamos estados
    this.registroOK = false;
    this.registroKO = false;
    this.errorMessage = ""; 
    
    // Validación de contraseñas local
    if (this.pwd1 !== this.pwd2) {
      this.registroKO = true;
      this.errorMessage = "Las contraseñas no coinciden";
      return;
    }

    // Llamada al servicio
    this.service.register(this.bar!, this.email!, this.pwd1!, this.pwd2!, this.clientId!, this.clientSecret!)
      .subscribe({
      next: () => {
        this.registroOK = true;
      },
      error: (err) => {
        this.registroKO = true;
        
        // 2. AQUÍ CAPTURAMOS EL MENSAJE REAL DEL BACKEND
        // Si Java lanza "Usuario ya registrado", Angular lo recibe en err.error.message o err.error
        this.errorMessage = err.error?.message || err.error || "Error desconocido en el registro";
        
        console.error('Error detallado:', err);
      }
    });
  }
}

