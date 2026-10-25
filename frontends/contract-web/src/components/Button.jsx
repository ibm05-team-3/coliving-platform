// CoLiv 버튼: 알약형(rounded-full). primary = 채운 버튼, outline = 흰 바탕 테두리 버튼
const VARIANTS = {
  primary: 'bg-primary text-white hover:bg-primary-strong disabled:bg-primary/40',
  outline: 'bg-surface text-ink border border-line hover:bg-soft',
  soft: 'bg-primary-soft text-ink hover:bg-soft',
};
const SIZES = {
  md: 'h-11 px-6 text-[15px]',
  sm: 'h-9 px-4 text-sm',
};

export default function Button({ variant = 'primary', size = 'md', className = '', ...props }) {
  return (
    <button
      type="button"
      className={`inline-flex items-center justify-center gap-1 rounded-full font-medium transition-colors disabled:cursor-not-allowed ${VARIANTS[variant]} ${SIZES[size]} ${className}`}
      {...props}
    />
  );
}
