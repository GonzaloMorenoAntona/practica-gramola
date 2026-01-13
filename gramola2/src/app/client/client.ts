import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SpotifyService } from '../spotify';
import { PaymentService } from '../payment-service';
import { Router } from '@angular/router';
import { UserService } from '../user';
import { PaymentComponent } from '../payments/payments'; //para poder usar el componente de pago en el HTML

@Component({
  selector: 'app-client',
  standalone: true,
  imports: [CommonModule, FormsModule, PaymentComponent],
  templateUrl: './client.html',
  styleUrls: ['./client.css']
})
export class ClientComponent implements OnInit, OnDestroy {
  
  @Input() isFullWidth: boolean = false; //dejarlo son con la cola y el buscador

  // Variables de Cola y Reproducción
  queue: any[] = [];
  currentTrack: any = null;
  refreshInterval: any;

  // Variables del Buscador
  searchQuery: string = '';
  searchResults: any[] = [];
  searchError?: string;

  // Variables para el Modal (YA NO HAY VARIABLES DE STRIPE AQUÍ)
  showPaymentModal: boolean = false;
  selectedTrack: any = null;
  songPrice: number = 0;

  constructor(
    private spoti: SpotifyService, 
    private paymentService: PaymentService, 
    private router: Router, 
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // cargar la cola inicial y activar el "Polling" (actualizar cada 5s)
    this.getRealQueue();
    this.refreshInterval = setInterval(() => {
      this.getRealQueue();
    }, 5000);

    // obtener el precio de la canción
    this.paymentService.getSongPrice().subscribe({
      next: (data: any) => {
        this.songPrice = data.value;
      },
      error: (e) => console.error("Error al obtener precio", e)
    });
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  // cola real de Spotify
  getRealQueue() {
    this.spoti.getUserQueue().subscribe({
      next: (data: any) => {
        this.currentTrack = data.currently_playing;//accede al objeto actualmente reproducido
        this.queue = data.queue;
      },
      error: (e) => console.error("Error obteniendo cola real:", e)
    });
  }

  // búsqueda de canciones
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

  // logica del Modal de Pago
  
  openPaymentModal(track: any) {
    this.selectedTrack = track;
    this.showPaymentModal = true; // muestra el modal, lo demas lo hace PaymentComponent
  }

  // Este método se ejecuta cuando el componente PaymentComponent nos avisa del éxito
  onSongPaid(track: any) {
    this.showPaymentModal = false; // Cerramos modal
    this.addSongToQueue(track);
    
    let barName = sessionStorage.getItem('barName') || 'Bar Desconocido';
    this.spoti.saveSongInDb(track, barName).subscribe();
  }

  addSongToQueue(track: any) {
    this.spoti.addToQueue(track.uri).subscribe({
      next: () => {
        console.log(`"${track.name}" añadida.`);
        this.clearSearch();
        
        // Esperamos 1.5s y actualizamos la cola visualmente
        setTimeout(() => {
          this.getRealQueue();
        }, 1500);
      },
      error: (e) => console.error("Error añadiendo a cola de Spotify", e)
    });
  }

  goToLogin() {
    this.userService.logout(); 
    this.router.navigate(['/login']);
  }
}