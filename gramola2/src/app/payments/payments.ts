import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { PaymentService } from '../payment-service';
import { SpotifyService } from '../spotify'; 
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms';

declare let Stripe: any

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payments.html',
  styleUrl: './payments.css'
})
export class PaymentComponent implements OnInit {

  // añadimos inputs y outputs para el modo canción
  @Input() mode: 'subscription' | 'song' = 'subscription';
  @Input() songTrack: any = null;
  @Input() songPrice: number = 0;
  
  @Output() close = new EventEmitter<void>();     // Para cerrar el modal
  @Output() songPaid = new EventEmitter<any>();   // Para avisar que se pagó, enviando la cancion

  stripe = new Stripe("pk_test_51SIV0yRm0ClsCnoVWXB3iOiEfdtda0z61OvJDYLWIIAq5FQZuIdFOAb4sEwtk8w2eEooAbJXOSKxsuGw3j56g5G900aYokx6Qx")
  transactionDetails: any;
  token? : string
  plans: any[] = [];
  selectedPlanId?: number;

  constructor(
    private PaymentService: PaymentService, 
    private router : Router,
    private spoti: SpotifyService 
  ) { }

  ngOnInit(): void {
    if (this.mode === 'subscription') {
        // MODO SUSCRIPCIÓN: Cargamos planes y token
        const params = this.router.parseUrl(this.router.url).queryParams;
        this.token = params['token'];
        this.PaymentService.getPlans().subscribe(data => this.plans = data);
    } else {
        // MODO CANCIÓN: Iniciamos el proceso directamente
        this.initSongPayment();
    }
  }

  // método para iniciar el pago de canción
  initSongPayment() {
    let emailBar = sessionStorage.getItem('barEmail') || '';
    this.spoti.prepareSongPayment(this.songTrack.name, emailBar).subscribe({
        next: (res: any) => {
            this.transactionDetails = { 
                data: { client_secret: res.clientSecret } 
            };
            
            //  Llamamos al metodo original showForm
            setTimeout(() => this.showForm(), 100);//con el timeout evitamos problemas de renderizado
        },
        error: (e) => alert("Error iniciando pago: " + e.message)
    });
  }

  prepay() {
    if (!this.selectedPlanId) return alert("Selecciona una suscripción");
    this.PaymentService.prepay(this.selectedPlanId, this.token!).subscribe({
      next: (response: any) => {
        this.transactionDetails = JSON.parse(response.body); 
        this.showForm(); 
      },
      error: (e) => alert("Error: " + (e.error?.message || e.message))
    });
  }
  //esta es la unica forma de que el input de la tarjeta se vea bonito y coincida, por cuestiones de seguridad
  showForm() {
    let elements = this.stripe.elements()
    let style = {
      base: {
        color: "#32325d", fontFamily: 'Arial, sans-serif',
        fontSmoothing: "antialiased", fontSize: "16px",
        "::placeholder": { color: "#32325d" }
      },
      invalid: {
        fontFamily: 'Arial, sans-serif', color: "#fa755a", iconColor: "#fa755a"
      }
    }
    
    // Creamos la tarjeta
    let card = elements.create("card", { style: style })
    card.mount("#card-element")
    
    // Listener de errores
    card.on("change", function (event: any) {
      const btn = document.querySelector("button#submit-btn") as HTMLButtonElement; // Ojo al ID
      if(btn) btn.disabled = event.empty;
      
      const errDiv = document.querySelector("#card-error");
      if(errDiv) errDiv.textContent = event.error ? event.error.message : "";
    });

    let self = this
    let form = document.getElementById("payment-form");
    
    // Clonamos el nodo o nos aseguramos de no acumular listeners si se abre/cierra muchas veces
    // Para simplificar, usamos tu lógica directa:
    if (form) {
        form.style.display = "block";
        
        // Sobrescribimos el onsubmit para que no se acumulen eventos
        form.onsubmit = function (event) {
            event.preventDefault();
            self.payWithCard(card);
        };
    }
  }

  payWithCard(card: any) {
    let self = this
    this.stripe.confirmCardPayment(this.transactionDetails.data.client_secret, {
      payment_method: { card: card }
    }).then(function (response: any) {
      if (response.error) {
        // Escribimos el error en el párrafo del HTML
        const errorElement = document.getElementById('card-error');
        if (errorElement) {
            errorElement.textContent = response.error.message;
            errorElement.style.display = 'block'; // Aseguramos que se vea
        }

      } else {
        if (response.paymentIntent.status === 'succeeded') {
            
            if (self.mode === 'subscription') {
                self.PaymentService.confirm(self.transactionDetails.id, self.token!).subscribe({
                    next: () => self.router.navigate(["/login"]),
                    error: (err) => alert("Error al confirmar: " + err.message)
                });
            } else {
                // MODO CANCIÓN: Avisamos al padre y cerramos
                self.songPaid.emit(self.songTrack);
                self.close.emit();
            }
        }
      }
    });
  }
}

