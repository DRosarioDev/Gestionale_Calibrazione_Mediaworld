import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { BrandLogoComponent } from '../../shared/components/brand-logo/brand-logo.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-pagina-non-trovata',
  standalone: true,
  imports: [RouterModule, BrandLogoComponent, IconComponent],
  templateUrl: './pagina-non-trovata.component.html',
  styleUrl: './pagina-non-trovata.component.css',
})
export class PaginaNonTrovataComponent {}
