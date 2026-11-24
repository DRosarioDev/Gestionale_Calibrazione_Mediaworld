import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
} from '@angular/core';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css',
})
export class ModalComponent implements OnChanges, OnDestroy {
  @Input() aperto = false;
  @Input() titolo = '';
  @Input() sottotitolo = '';
  @Input() larghezza = '580px';
  @Output() chiuso = new EventEmitter<void>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['aperto']) {
      document.body.style.overflow = this.aperto ? 'hidden' : '';
    }
  }

  ngOnDestroy(): void {
    document.body.style.overflow = '';
  }

  chiudi(): void {
    this.chiuso.emit();
  }
}
