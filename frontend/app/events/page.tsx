"use client";

import { motion } from "framer-motion";
import { Search, Calendar, MapPin, ArrowRight, Filter, Heart } from "lucide-react";
import Link from "next/link";
import { useState, useEffect } from "react";
import api from "@/lib/axios";
import { useRouter } from "next/navigation";
import { toast } from "react-hot-toast";

interface Event {
  id: number;
  title: string;
  venue: string;
  eventDate: string;
  ticketCategories?: { price: number }[];
  category: string;
}

export default function EventsPage() {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const router = useRouter();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const res = await api.get('/events');
        setEvents(res.data);
      } catch (err) {
        console.error("Failed to load events", err);
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  const filteredEvents = events.filter(e => {
    const isFuture = new Date(e.eventDate) >= new Date();
    const matchesSearch = e.title.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          e.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          e.venue.toLowerCase().includes(searchTerm.toLowerCase());
    return isFuture && matchesSearch;
  });

  const getCategoryIcon = (category: string) => {
    switch(category.toLowerCase()) {
      case 'music': return <Music size={64} className="text-primary/50" />;
      case 'technology': return <Zap size={64} className="text-accent/50" />;
      default: return <Star size={64} className="text-yellow-500/50" />;
    }
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Header Section */}
      <section className="bg-card border-b border-border py-16 relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/5 to-accent/5 opacity-50"></div>
        <div className="container mx-auto px-6 relative z-10 text-center">
          <motion.h1 
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-4xl md:text-5xl font-extrabold mb-4"
          >
            Explore <span className="text-primary">Events</span>
          </motion.h1>
          <motion.p 
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="text-foreground/70 max-w-2xl mx-auto mb-8"
          >
            Find the best tech conferences, music festivals, and networking events happening around you.
          </motion.p>
          
          <motion.div 
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="max-w-3xl mx-auto flex flex-col md:flex-row gap-4"
          >
            <div className="relative flex-1">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-foreground/40" size={20} />
              <input 
                type="text" 
                placeholder="Search by event name, category, or venue..." 
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-12 pr-4 py-4 bg-background rounded-full outline-none focus:ring-2 focus:ring-primary transition-shadow border border-border text-foreground"
              />
            </div>
            <button className="px-6 py-4 bg-secondary text-secondary-foreground rounded-full border border-border hover:bg-border transition-colors flex items-center justify-center gap-2 font-medium">
              <Filter size={18} /> Filters
            </button>
          </motion.div>
        </div>
      </section>

      {/* Events Grid */}
      <section className="py-16 container mx-auto px-6">
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[1, 2, 3, 4, 5, 6].map(i => (
              <div key={i} className="h-96 rounded-3xl bg-secondary animate-pulse"></div>
            ))}
          </div>
        ) : filteredEvents.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {filteredEvents.map((event, i) => (
              <motion.div 
                key={event.id}
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: i * 0.05, duration: 0.4 }}
                onClick={() => router.push(`/events/${event.id}`)}
                className="group rounded-3xl border border-border bg-card overflow-hidden hover:shadow-2xl hover:shadow-primary/10 transition-all hover:-translate-y-2 cursor-pointer flex flex-col"
              >
                <div className="h-56 bg-gradient-to-br from-secondary/50 via-background to-border relative overflow-hidden flex items-center justify-center">
                   {(event as any).venueImageUrl ? (
                     <img src={`http://localhost:8080${(event as any).venueImageUrl.startsWith('/') ? '' : '/uploads/events/'}${(event as any).venueImageUrl}`} alt="Venue" className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
                   ) : (
                     <div className="w-full h-full flex items-center justify-center bg-gradient-to-tr from-primary/20 to-accent/20 group-hover:scale-110 transition-transform duration-700">
                        {getCategoryIcon(event.category || '')}
                        <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-10 mix-blend-overlay"></div>
                     </div>
                   )}
                   <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-background/20 to-transparent"></div>
                   
                   <div className="absolute top-4 left-4 px-3 py-1 bg-background/80 backdrop-blur-md rounded-full text-xs font-semibold">
                     {event.category}
                   </div>

                   <div className="absolute top-4 right-16 px-4 py-1 bg-primary text-primary-foreground rounded-full text-xs font-bold shadow-[0_0_15px_rgba(139,92,246,0.5)] flex items-center gap-2">
                     <Calendar size={12} />
                     {new Date(event.eventDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
                   </div>

                   <button 
                     onClick={(e) => {
                       e.stopPropagation();
                       const saved = JSON.parse(localStorage.getItem('savedEvents') || '[]');
                       const isSaved = saved.find((item: any) => item.id === event.id);
                       let newSaved;
                       if (isSaved) {
                         newSaved = saved.filter((item: any) => item.id !== event.id);
                         toast.success("Removed from saved events!");
                       } else {
                         newSaved = [...saved, event];
                         toast.success("Saved to your wishlist!");
                       }
                       localStorage.setItem('savedEvents', JSON.stringify(newSaved));
                     }}
                     className="absolute top-4 right-4 h-8 w-8 bg-background/80 backdrop-blur-md rounded-full flex items-center justify-center text-foreground/50 hover:text-pink-500 hover:bg-background transition-all z-10"
                   >
                     <Heart size={16} />
                   </button>
                </div>
                <div className="p-6 flex-1 flex flex-col -mt-4 relative z-10 bg-card rounded-t-3xl">
                  <h3 className="text-2xl font-bold mb-2 group-hover:text-primary transition-colors">{event.title}</h3>
                  
                  <div className="space-y-3 mb-6 text-foreground/70 text-sm">
                    <div className="flex items-center gap-3 bg-secondary/30 p-2 rounded-xl">
                      <Calendar size={18} className="text-primary" />
                      <span className="font-medium text-foreground">{new Date(event.eventDate).toLocaleString(undefined, { weekday: 'long', hour: '2-digit', minute: '2-digit' })}</span>
                    </div>
                    <div className="flex items-center gap-3 bg-secondary/30 p-2 rounded-xl">
                      <MapPin size={18} className="text-accent" />
                      <span className="font-medium text-foreground">{event.venue}</span>
                    </div>
                  </div>

                  <div className="mt-auto flex items-center justify-between pt-4 border-t border-border">
                    <div>
                      <p className="text-xs text-foreground/50 uppercase font-semibold tracking-wider">Starting from</p>
                      <p className="text-2xl font-black text-foreground">
                        Rs. {event.ticketCategories?.length ? Math.min(...event.ticketCategories.map(tc => tc.price)).toLocaleString() : "0"}
                      </p>
                    </div>
                    <button className="h-12 w-12 rounded-full bg-primary/10 text-primary flex items-center justify-center group-hover:bg-primary group-hover:text-primary-foreground transition-all group-hover:rotate-45">
                      <ArrowRight size={20} />
                    </button>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        ) : (
          <div className="text-center py-20">
            <div className="inline-flex h-20 w-20 rounded-full bg-secondary items-center justify-center mb-6">
              <Search size={32} className="text-foreground/40" />
            </div>
            <h2 className="text-2xl font-bold mb-2">No events found</h2>
            <p className="text-foreground/60 max-w-md mx-auto">We couldn't find any events matching "{searchTerm}". Try adjusting your search criteria.</p>
          </div>
        )}
      </section>
    </div>
  );
}
