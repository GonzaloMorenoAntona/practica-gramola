import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SpotifyService } from '../spotify';
import { PaymentService } from '../payment-service';
import { Router } from '@angular/router'; // IMPORTANTE
import { UserService } from '../user';

declare var Stripe: any;

@Component({
  selector: 'app-client', // Nombre del selector para usarlo luego como <app-client></app-client>
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client.html',
  styleUrls: ['./client.css']
})
export class ClientComponent implements OnInit, OnDestroy {
  
  @Input() isFullWidth: boolean = false; // Para saber si ocupamos todo el ancho (modo cliente) o no (modo dueño)

  // Variables de Cola y Reproducción
  queue: any[] = [];
  currentTrack: any = null;
  refreshInterval: any;

  // Variables del Buscador
  searchQuery: string = '';
  searchResults: any[] = [];
  searchError?: string;

  // Variables de Pagos (Stripe)
  stripe: any;
  elements: any;
  card: any;
  showPaymentModal: boolean = false;
  selectedTrack: any = null;
  isProcessing: boolean = false;
  paymentError: string = '';
  songPrice: number = 0;

  
  
  // Tu clave pública de Stripe
  stripePublicKey = 'pk_test_51SIV0yRm0ClsCnoVWXB3iOiEfdtda0z61OvJDYLWIIAq5FQZuIdFOAb4sEwtk8w2eEooAbJXOSKxsuGw3j56g5G900aYokx6Qx';

  constructor(private spoti: SpotifyService, private paymentService: PaymentService, private router: Router, private userService: UserService) {}

  ngOnInit(): void {
    // 1. Inicializar Stripe
    if (typeof Stripe !== 'undefined') {
      this.stripe = Stripe(this.stripePublicKey);
    }

    // 2. Cargar la cola inicial y activar el "Polling" (actualizar cada 5s)
    this.getRealQueue();
    this.refreshInterval = setInterval(() => {
      this.getRealQueue();
    }, 5000);

    this.paymentService.getSongPrice().subscribe({
      next: (data: any) => {
        this.songPrice = data.value; // Guardamos el valor (ej: 1.00)
        console.log("Precio actual de la canción:", this.songPrice);
      },
      error: (e) => console.error("Error al obtener precio", e)
    });
  }

  ngOnDestroy(): void {
    // Apagar el reloj si salimos del componente
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  // --- LÓGICA DE COLA REAL ---
  getRealQueue() {
    this.spoti.getUserQueue().subscribe({
      next: (data: any) => {
        // Spotify nos da lo que suena YA y lo que viene DESPUÉS
        this.currentTrack = data.currently_playing;
        this.queue = data.queue;
      },
      error: (e) => console.error("Error obteniendo cola real:", e)
    });
  }

  // --- LÓGICA DEL BUSCADOR ---
  search() {
    if (!this.searchQuery) return;
    this.searchError = undefined;
    
    this.spoti.search(this.searchQuery).subscribe({
      next: (res: any) => {
         if(res.tracks) this.searchResults = res.tracks.items;
      },
      error: () => this.searchError = "Error al buscar la canción."
    });
  }

  clearSearch() {
    this.searchQuery = '';
    this.searchResults = [];
    this.searchError = undefined;
  }

  // --- LÓGICA DE PAGOS ---
  openPaymentModal(track: any) {
    this.selectedTrack = track;
    this.showPaymentModal = true;
    this.paymentError = '';

    // Esperamos un pelín para que el HTML del modal exista antes de montar Stripe
    setTimeout(() => {
      if (!this.elements) {
        this.elements = this.stripe.elements();
        this.card = this.elements.create('card');
      }
      this.card.mount('#card-element');
    }, 100);
  }

  cancelPayment() {
    this.showPaymentModal = false;
  }

  async confirmPayment() {
    this.isProcessing = true;
    this.paymentError = '';

    // 1. Pedir 'ClientSecret' a tu Backend Java
    this.spoti.prepareSongPayment(this.selectedTrack.name).subscribe({
      next: async (res: any) => {
        
        // 2. Confirmar pago con Stripe en el Front
        const result = await this.stripe.confirmCardPayment(res.clientSecret, {
          payment_method: { card: this.card }
        });

        if (result.error) {
          this.paymentError = result.error.message;
          this.isProcessing = false;
        } else {
          if (result.paymentIntent.status === 'succeeded') {
            // 3. ¡Pago Éxitoso! -> Añadir a Spotify y BD
            this.addSongToQueue(this.selectedTrack);
            
            let barName = sessionStorage.getItem('barName') || 'Bar Desconocido';
            this.spoti.saveSongInDb(this.selectedTrack, barName).subscribe();

            this.cancelPayment();
            this.isProcessing = false;
          }
        }
      },
      error: () => {
        this.paymentError = 'Error de conexión con el servidor.';
        this.isProcessing = false;
      }
    });
  }

  addSongToQueue(track: any) {
    this.spoti.addToQueue(track.uri).subscribe({
      next: () => {
        console.log(`✅ "${track.name}" añadida.`);
        this.clearSearch(); // Limpiamos el buscador para ver la cola
        
        // Esperamos 1.5s y actualizamos la cola visualmente
        setTimeout(() => {
          this.getRealQueue();
        }, 1500);
      },
      error: (e) => console.error("Error añadiendo a cola de Spotify", e)
    });
  }
  goToLogin() {
    // 1. Cerramos sesión (opcional, pero recomendable por seguridad)
    // Esto hará que aparezcan "Login" y "Registrar" en la barra de arriba
    this.userService.logout(); 

    // 2. Navegamos a la pantalla de login
    this.router.navigate(['/login']);
  }
}