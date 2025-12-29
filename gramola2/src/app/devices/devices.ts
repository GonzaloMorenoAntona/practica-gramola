import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SpotifyService } from '../spotify';


@Component({
  selector: 'app-devices',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './devices.html', // <--- Enlace al HTML separado
  styleUrls: ['./devices.css']   // <--- Enlace al CSS separado
})
export class DevicesComponent implements OnInit {
  devices: any[] = [];
  deviceError?: string;

  constructor(private spoti: SpotifyService) {}

  ngOnInit() {
    this.getDevices();
  }

  getDevices() {
    this.deviceError = undefined;
    this.spoti.getDevices().subscribe({
      next: (res: any) => {
        this.devices = res.devices;
        if (!this.devices.length) this.deviceError = "No hay dispositivos.";
      },
      error: (e) => this.deviceError = "Error cargando dispositivos"
    });
  }
}