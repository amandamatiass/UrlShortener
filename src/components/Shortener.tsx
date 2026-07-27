import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { LinkIcon, ArrowRightIcon, Loader2Icon, ZapIcon } from 'lucide-react';
import { ResultCard } from './ResultCard';

export interface ShortLink {
  id: string;
  original: string;
  code: string;
  short: string;
  createdAt: number;
}

const DOMAIN = 'zap.li';

function generateCode(len = 6): string {
  const chars = 'abcdefghijkmnpqrstuvwxyz23456789';
  let out = '';
  for (let i = 0; i < len; i++) {
    out += chars[Math.floor(Math.random() * chars.length)];
  }
  return out;
}

function normalizeUrl(value: string): string {
  const trimmed = value.trim();
  if (!/^https?:\/\//i.test(trimmed)) {
    return `https://${trimmed}`;
  }
  return trimmed;
}

function isValidUrl(value: string): boolean {
  try {
    const url = new URL(normalizeUrl(value));
    return !!url.hostname && url.hostname.includes('.');
  } catch {
    return false;
  }
}

export function Shortener() {
  const [input, setInput] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [links, setLinks] = useState<ShortLink[]>([]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (loading) return;
    setError(null);

    if (!input.trim()) {
      setError('Cole um link para encurtar.');
      return;
    }
    if (!isValidUrl(input)) {
      setError('Esse link não parece válido. Verifique e tente de novo.');
      return;
    }

    setLoading(true);
    // Simula o processamento no cliente
    setTimeout(() => {
      const code = generateCode();
      const newLink: ShortLink = {
        id: `${Date.now()}`,
        original: normalizeUrl(input),
        code,
        short: `${DOMAIN}/${code}`,
        createdAt: Date.now()
      };
      setLinks((prev) => [newLink, ...prev]);
      setInput('');
      setLoading(false);
    }, 700);
  };

  return (
    <main className="w-full min-h-full px-5 py-8 sm:px-8 sm:py-10">
      <div className="mx-auto flex w-full max-w-3xl flex-col">
        <header className="flex items-center justify-between" aria-label="Zap.li">
          <span className="flex items-center gap-2 text-base font-bold tracking-tight text-slate-900">
            <span className="flex h-7 w-7 items-center justify-center rounded-md bg-[var(--accent)] text-sm text-white"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-unlink-icon lucide-unlink"><path d="m18.84 12.25 1.72-1.71h-.02a5.004 5.004 0 0 0-.12-7.07 5.006 5.006 0 0 0-6.95 0l-1.72 1.71"/><path d="m5.17 11.75-1.71 1.71a5.004 5.004 0 0 0 .12 7.07 5.006 5.006 0 0 0 6.95 0l1.71-1.71"/><line x1="8" x2="8" y1="2" y2="5"/><line x1="2" x2="5" y1="8" y2="8"/><line x1="16" x2="16" y1="19" y2="22"/><line x1="19" x2="22" y1="16" y2="16"/></svg></span>
            Url Shortener
          </span>
        </header>

        <section className="mx-auto flex w-full max-w-2xl flex-1 flex-col items-center pt-24 text-center sm:pt-32">
          <div className="flex items-center gap-2 text-sm font-medium text-[var(--accent)]">
            <ZapIcon className="h-4 w-4" />
            Encurte em segundos
          </div>
          <h1 className="mt-5 text-4xl font-semibold tracking-[-0.045em] text-slate-900 sm:text-5xl">
            Um link menor para
            <br />
            compartilhar melhor.
          </h1>
          <p className="mt-5 max-w-md text-base leading-7 text-slate-500">
            Cole sua URL e crie um link curto, simples e pronto para enviar.
          </p>

          <form onSubmit={handleSubmit} className="mt-10 w-full text-left">
            <div
              className={`flex flex-col gap-2 rounded-xl border bg-white p-2 transition-all sm:flex-row sm:items-center ${
              error ?
              'border-red-300' :
              'border-slate-200 focus-within:border-[var(--accent)] focus-within:ring-4 focus-within:ring-teal-50'}`
              }>
              
              <div className="flex min-w-0 flex-1 items-center gap-3 px-3">
                <LinkIcon className="h-5 w-5 shrink-0 text-slate-400" />
                <input
                  type="text"
                  inputMode="url"
                  value={input}
                  onChange={(e) => {
                    setInput(e.target.value);
                    if (error) setError(null);
                  }}
                  placeholder="cole.seu.link/aqui"
                  aria-label="Link para encurtar"
                  aria-invalid={!!error}
                  className="w-full bg-transparent py-3 text-base text-slate-900 placeholder-slate-400 outline-none" />
                
              </div>
              <button
                type="submit"
                disabled={loading}
                className="flex items-center justify-center gap-2 rounded-lg bg-[var(--accent)] px-5 py-3 text-sm font-semibold text-white transition-colors hover:bg-[var(--accent-hover)] focus:outline-none focus:ring-4 focus:ring-teal-100 disabled:cursor-not-allowed disabled:opacity-70">
                
                {loading ?
                <>
                    <Loader2Icon className="h-4 w-4 animate-spin" />
                    Encurtando
                  </> :

                <>
                    Encurtar
                    <ArrowRightIcon className="h-4 w-4" />
                  </>
                }
              </button>
            </div>
            <AnimatePresence>
              {error &&
              <motion.p
                initial={{ opacity: 0, y: -4 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0 }}
                role="alert"
                className="mt-3 text-sm text-red-600">
                
                  {error}
                </motion.p>
              }
            </AnimatePresence>
          </form>

          <section className="mt-8 w-full text-left" aria-live="polite">
            <AnimatePresence initial={false}>
              {links.map((link) =>
              <motion.div
                key={link.id}
                layout
                initial={{ opacity: 0, y: 12, scale: 0.98 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                exit={{ opacity: 0, scale: 0.98 }}
                transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                className="mb-3">
                
                  <ResultCard link={link} />
                </motion.div>
              )}
            </AnimatePresence>

            {links.length === 0 && !loading &&
            <p className="text-center text-sm text-slate-400">Seu link encurtado aparecerá aqui.</p>
            }
          </section>
        </section>

      </div>
    </main>);

}