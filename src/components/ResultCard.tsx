import React, { useState } from 'react';
import { CopyIcon, CheckIcon, ExternalLinkIcon } from 'lucide-react';
import type { ShortLink } from './Shortener';

interface ResultCardProps {
  link: ShortLink;
}

export function ResultCard({ link }: ResultCardProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(`https://${link.short}`);
    } catch {

      // Fallback silencioso
    }setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  };

  return (
    <div className="flex flex-col gap-4 rounded-xl border border-slate-200 bg-white p-4 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0 flex-1">
        <p className="truncate text-xs text-slate-400" title={link.original}>
          {link.original}
        </p>
        <p className="mt-1.5 font-mono text-base font-medium text-[var(--accent)]">
          {link.short}
        </p>
      </div>

      <div className="flex items-center gap-2">
        <a
          href={link.original}
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Abrir link original"
          className="flex h-10 w-10 items-center justify-center rounded-lg border border-slate-200 text-slate-500 transition-colors hover:bg-slate-50 hover:text-slate-900 focus:outline-none focus:ring-4 focus:ring-teal-50">
          
          <ExternalLinkIcon className="h-4 w-4" />
        </a>
        <button
          onClick={handleCopy}
          aria-label="Copiar link curto"
          className={`flex h-10 items-center justify-center gap-2 rounded-lg px-4 text-sm font-semibold transition-colors focus:outline-none focus:ring-4 focus:ring-teal-50 ${
          copied ?
          'bg-teal-50 text-teal-700' :
          'bg-slate-900 text-white hover:bg-[var(--accent)]'}`
          }>
          
          {copied ?
          <>
              <CheckIcon className="h-4 w-4" />
              Copiado
            </> :

          <>
              <CopyIcon className="h-4 w-4" />
              Copiar
            </>
          }
        </button>
      </div>
    </div>);

}