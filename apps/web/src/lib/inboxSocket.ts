import { useToastStore } from '../stores/useToastStore';
import { queryClient } from './queryClient';

export const initInboxSocket = () => {
  console.log('[WebSocket] Connecting to ws://localhost:8080/inbox/stream...');
  
  const timeoutId = setTimeout(() => {
    console.log('[WebSocket] Connection established successfully.');
    
    const intervalId = setInterval(() => {
      const mockSender = ['Alice Johnson', 'Mark Spencer', 'Sarah Connor'][Math.floor(Math.random() * 3)];
      const mockText = [
        'Hi, can I get more info about the subscription plan?',
        'Your product looks awesome! Do you have a promo code?',
        'Is the customer support active 24/7?'
      ][Math.floor(Math.random() * 3)];
      
      console.log('[WebSocket] Received new_inbox_message event:', { sender: mockSender, text: mockText });
      
      useToastStore.getState().addToast(`New message from ${mockSender}: "${mockText}"`, 'info');
      queryClient.invalidateQueries({ queryKey: ['inboxMessages'] });
    }, 40000);
    
    return () => clearInterval(intervalId);
  }, 2000);

  return () => {
    clearTimeout(timeoutId);
  };
};
