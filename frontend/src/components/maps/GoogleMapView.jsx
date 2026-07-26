import { useEffect, useRef, useState } from 'react';
import { loadGoogleMapsScript } from '../../utils/googleMapsLoader';
import TrackingMap from '../tracking/TrackingMap';

const DARK_MAP_STYLE = [
  { elementType: 'geometry', stylers: [{ color: '#0f172a' }] },
  { elementType: 'labels.text.stroke', stylers: [{ color: '#0f172a' }] },
  { elementType: 'labels.text.fill', stylers: [{ color: '#94a3b8' }] },
  { featureType: 'administrative', elementType: 'geometry', stylers: [{ color: '#334155' }] },
  { featureType: 'road', elementType: 'geometry', stylers: [{ color: '#1e293b' }] },
  { featureType: 'road.highway', elementType: 'geometry', stylers: [{ color: '#312e81' }] },
  { featureType: 'water', elementType: 'geometry', stylers: [{ color: '#0284c7' }] },
];

export default function GoogleMapView({
  origin = 'Origin Pickup',
  originCoords = { lat: 37.7749, lng: -122.4194 },
  destination = 'Destination Address',
  destinationCoords = { lat: 37.7833, lng: -122.4167 },
  driverPosition = { lat: 37.779, lng: -122.418, speed: 65 },
  status = 'IN_TRANSIT',
  distanceRemaining = '14.2 km',
  eta = '28 mins',
}) {
  const mapRef = useRef(null);
  const [mapLoaded, setMapLoaded] = useState(false);
  const [isFallback, setIsFallback] = useState(false);

  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

  useEffect(() => {
    if (!apiKey) {
      setIsFallback(true);
      return;
    }

    loadGoogleMapsScript(apiKey)
      .then((maps) => {
        if (!mapRef.current) return;

        const map = new maps.Map(mapRef.current, {
          center: driverPosition,
          zoom: 13,
          styles: DARK_MAP_STYLE,
          disableDefaultUI: true,
          zoomControl: true,
        });

        // 1. Origin Marker
        new maps.Marker({
          position: originCoords,
          map,
          title: `Origin: ${origin}`,
          icon: {
            url: 'https://maps.google.com/mapfiles/ms/icons/green-dot.png',
          },
        });

        // 2. Destination Marker
        new maps.Marker({
          position: destinationCoords,
          map,
          title: `Destination: ${destination}`,
          icon: {
            url: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',
          },
        });

        // 3. Driver Marker
        new maps.Marker({
          position: driverPosition,
          map,
          title: `Driver on Route (${driverPosition.speed || 60} km/h)`,
          icon: {
            url: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png',
          },
        });

        // 4. Route Polyline
        const routePath = new maps.Polyline({
          path: [originCoords, driverPosition, destinationCoords],
          geodesic: true,
          strokeColor: '#6366f1',
          strokeOpacity: 0.8,
          strokeWeight: 4,
        });

        routePath.setMap(map);
        setMapLoaded(true);
      })
      .catch(() => {
        setIsFallback(true);
      });
  }, [apiKey, driverPosition, originCoords, destinationCoords]);

  if (isFallback || !apiKey) {
    return (
      <TrackingMap
        origin={origin}
        destination={destination}
        driverPosition={driverPosition}
        status={status}
        distanceRemaining={distanceRemaining}
        eta={eta}
      />
    );
  }

  return (
    <div className="relative w-full h-[380px] bg-surface-900 border border-white/10 rounded-3xl overflow-hidden shadow-2xl">
      <div ref={mapRef} className="w-full h-full" />
      {!mapLoaded && (
        <div className="absolute inset-0 flex items-center justify-center bg-surface-900/90 text-xs text-gray-400">
          <div className="w-6 h-6 border-2 border-primary-500/30 border-t-primary-500 rounded-full animate-spin mr-2" />
          Initializing Google Maps...
        </div>
      )}
    </div>
  );
}
