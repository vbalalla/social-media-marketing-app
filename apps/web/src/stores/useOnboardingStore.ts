import { create } from 'zustand';

interface OnboardingState {
  onboardingComplete: boolean;
  setOnboardingComplete: (val: boolean) => void;
}

const storedOnboarding = localStorage.getItem('serendia_onboarding_complete');

export const useOnboardingStore = create<OnboardingState>((set) => ({
  onboardingComplete: storedOnboarding === 'true',
  setOnboardingComplete: (val) => {
    localStorage.setItem('serendia_onboarding_complete', String(val));
    set({ onboardingComplete: val });
  }
}));
