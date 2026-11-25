import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ParametroCalibrazione } from '../../../../core/models/calibrazione.model';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';

export type ModalitaImportParametri = 'sostituisci' | 'aggiungi';

@Component({
  selector: 'app-importa-parametri-modal',
  standalone: true,
  imports: [ModalComponent, IconComponent],
  templateUrl: './importa-parametri-modal.component.html',
  styleUrl: './importa-parametri-modal.component.css',
})
export class ImportaParametriModalComponent {
  @Input() aperto = false;
  @Input() nomeFile = '';
  @Input() parametri: ParametroCalibrazione[] = [];
  @Input() righeIgnorate = 0;
  @Output() chiuso = new EventEmitter<void>();
  @Output() confermato = new EventEmitter<ModalitaImportParametri>();

  chiudi(): void {
    this.chiuso.emit();
  }

  applica(modalita: ModalitaImportParametri): void {
    this.confermato.emit(modalita);
  }
}
