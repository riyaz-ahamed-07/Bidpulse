import { useState, useEffect } from 'react';

const CountdownTimer = ({ startTime, endTime, status }) => {
  const [timeLeft, setTimeLeft] = useState('');

  useEffect(() => {
    if (status === 'ENDED') {
      setTimeLeft('Auction Closed');
      return;
    }

    const calculateTimeLeft = () => {
      const now = new Date().getTime();
      
      // Treat both DRAFT and SCHEDULED as upcoming auctions
      const isUpcoming = status === 'SCHEDULED' || status === 'DRAFT';
      
      const targetTime = isUpcoming ? new Date(startTime).getTime() : new Date(endTime).getTime();
      const difference = targetTime - now;

      if (difference <= 0) {
        return isUpcoming ? 'Starting now...' : 'Ending now...';
      }

      const days = Math.floor(difference / (1000 * 60 * 60 * 24));
      const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((difference % (1000 * 60)) / 1000);

      let timeString = '';
      if (days > 0) timeString += `${days}d `;
      if (hours > 0) timeString += `${hours}h `;
      timeString += `${minutes}m ${seconds}s`;

      return `${isUpcoming ? 'Starts in: ' : 'Ends in: '} ${timeString}`;
    };

    setTimeLeft(calculateTimeLeft());
    const timerId = setInterval(() => {
      setTimeLeft(calculateTimeLeft());
    }, 1000);

    return () => clearInterval(timerId);
  }, [startTime, endTime, status]);

  return (
    <div style={{ 
      display: 'inline-block', 
      padding: '4px 8px', 
      borderRadius: '6px', 
      fontSize: '0.9rem',
      fontWeight: 'bold',
      background: status === 'RUNNING' ? 'rgba(239, 68, 68, 0.1)' : 'rgba(99, 102, 241, 0.1)',
      color: status === 'RUNNING' ? 'var(--danger)' : 'var(--primary)'
    }}>
      ⏱️ {timeLeft}
    </div>
  );
};

export default CountdownTimer;